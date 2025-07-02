package com.olafura.jsondiff

import play.api.libs.json._
import com.olafura.jsondiff.Myers

case class ArrayAcc(count: Int, deletedCount: Int, acc: Map[String, JsValue])

class JsonDiff(oldJson: JsValue, newJson: JsValue):
  def diff(): JsValue = doDiff(oldJson, newJson)

  def splitUnderscoreMap(key: String, value: JsValue): Boolean =
    (key, value) match
      case (s"_${_}", JsArray(Seq(JsObject(_), JsNumber(0), JsNumber(0)))) =>
        true
      case _ => false

  def allChecked(
      list: List[(String, JsValue)],
      deletedMap: Map[String, JsValue]
  ): List[(String, JsValue)] = (list, deletedMap) match
    case (Nil, deletedMap) => deletedMap.toList
    case (head :: tail, deletedMap) =>
      head match
        case (i: String, Seq(value: JsValue)) if value.isInstanceOf[JsObject] =>
          val negI = s"_${i}"
          deletedMap.get(negI) match
            case Some(Seq(value2: JsValue, JsNumber(0), JsNumber(0))) =>
              (i, doDiff(value2, value)) :: allChecked(tail, deletedMap - negI)
            case None =>
              head :: allChecked(tail, deletedMap)
        case _ =>
          head :: allChecked(tail, deletedMap)

  def doDiff(a: JsValue, b: JsValue): JsValue = (a, b) match
    case (JsArray(l1), JsArray(l2)) =>
      val arrayAcc = Myers(l1, l2)
        .diff()
        .foldLeft(ArrayAcc(0, 0, Map.empty[String, JsValue])) { (acc, diff) =>
          diff match
            case Equals(equal) =>
              val equalLength = equal.length
              ArrayAcc(
                acc.count + equalLength,
                acc.deletedCount + equalLength,
                acc.acc
              )
            case Delete(deleted) =>
              deleted.foldLeft(acc) { (acc2, deletedItem) =>
                ArrayAcc(
                  acc2.count,
                  acc2.deletedCount + 1,
                  acc2.acc + (s"_${acc2.deletedCount}" -> JsArray(
                    Seq(
                      deletedItem.asInstanceOf[JsValue],
                      JsNumber(0),
                      JsNumber(0)
                    )
                  ))
                )
              }
            case Insert(inserted) =>
              inserted.foldLeft(acc) { (acc2, insertedItem) =>
                ArrayAcc(
                  acc2.count + 1,
                  acc2.deletedCount,
                  acc2.acc + (s"${acc2.count}" -> JsArray(
                    Seq(insertedItem.asInstanceOf[JsValue])
                  ))
                )
              }
        }

      val (deleted, checked) = arrayAcc.acc.partition(splitUnderscoreMap)
      val diff =
        if deleted.isEmpty && checked.isEmpty then arrayAcc.acc
        else if deleted.isEmpty then arrayAcc.acc
        else
          val checkedList =
            allChecked(checked.toList, deleted).filter((_, v) => v != JsNull)
          checkedList.toMap

      if diff.isEmpty then JsNull
      else JsObject(diff + ("_t" -> JsString("a")))

    case (JsObject(o1), JsObject(o2)) =>
      val keysNonUniq = o1.keySet ++ o2.keySet

      val diff =
        keysNonUniq.toSeq
          .map { k =>
            (
              k,
              (o1.get(k), o2.get(k)) match {
                case (Some(v1), Some(v2)) => doDiff(v1, v2)
                case (Some(v1), None) =>
                  JsArray(Seq(v1, JsNumber(0), JsNumber(0)))
                case (None, Some(v2)) => JsArray(Seq(v2))
                case (None, None)     => JsNull
              }
            )
          }
          .filterNot { case (_, v) => v == JsNull }
          .toMap

      if diff.isEmpty then JsNull
      else JsObject(diff)

    case (a, b) if a == b => JsNull
    case (a, b)           => JsArray(Seq(a, b))
