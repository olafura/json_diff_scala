package com.olafura.jsondiff

import org.scalatest._
import flatspec._
import matchers.should.Matchers
import play.api.libs.json._

class DiffSpec extends AnyFlatSpec with Matchers {

  private def diffJson(json1: String, json2: String): JsValue = {
    val j1 = Json.parse(json1)
    val j2 = Json.parse(json2)
    new JsonDiff(j1, j2).diff()
  }

  "JsonDiff" should "handle basic diff" in {
    val s1 = """{"1": 1}"""
    val s2 = """{"1": 2}"""
    val expected = Json.obj("1" -> Json.arr(1, 2))
    diffJson(s1, s2) shouldBe expected
  }

  it should "handle array diff" in {
    val s1 = """{"1": [1,2,3]}"""
    val s2 = """{"1": [1,2,4]}"""
    val expected = Json.obj(
      "1" -> Json.obj(
        "2" -> Json.arr(4),
        "_2" -> Json.arr(3, 0, 0),
        "_t" -> "a"
      )
    )
    diffJson(s1, s2) shouldBe expected
  }

  it should "handle same object" in {
    val s1 = """{"1": [1,2,3], "2": 1}"""
    val j1 = Json.parse(s1)
    new JsonDiff(j1, j1).diff() shouldBe Json.obj()
  }

  it should "handle object diff not changed" in {
    val s1 = """{"1": 1, "2": 2}"""
    val s2 = """{"1": 2, "2": 2}"""
    val expected = Json.obj("1" -> Json.arr(1, 2))
    diffJson(s1, s2) shouldBe expected
  }

  it should "handle array diff not changed" in {
    val s1 = """{"1": 1, "2": [1]}"""
    val s2 = """{"1": 2, "2": [1]}"""
    val expected = Json.obj("1" -> Json.arr(1, 2))
    diffJson(s1, s2) shouldBe expected
  }

  it should "handle array diff all changed" in {
    val s1 = """{"1": [1,2,3]}"""
    val s2 = """{"1": [4,5,6]}"""
    val expected = Json.obj(
      "1" -> Json.obj(
        "0" -> Json.arr(4),
        "1" -> Json.arr(5),
        "2" -> Json.arr(6),
        "_0" -> Json.arr(1, 0, 0),
        "_1" -> Json.arr(2, 0, 0),
        "_2" -> Json.arr(3, 0, 0),
        "_t" -> "a"
      )
    )
    diffJson(s1, s2) shouldBe expected
  }

  it should "handle array diff delete first" in {
    val s1 = """{"1": [1,2,3]}"""
    val s2 = """{"1": [2,3]}"""
    val expected = Json.obj(
      "1" -> Json.obj(
        "_0" -> Json.arr(1, 0, 0),
        "_t" -> "a"
      )
    )
    diffJson(s1, s2) shouldBe expected
  }

  it should "handle array diff shift one" in {
    val s1 = """{"1": [1,2,3]}"""
    val s2 = """{"1": [0,1,2,3]}"""
    val expected = Json.obj(
      "1" -> Json.obj(
        "0" -> Json.arr(0),
        "_t" -> "a"
      )
    )
    diffJson(s1, s2) shouldBe expected
  }

  it should "handle array diff with duplicate values" in {
    val s1 = """{"1": [1,2,1,3,3,2]}"""
    val s2 = """{"1": [3,1,2,1,2,3,3,2,1]}"""
    val expected = Json.obj(
      "1" -> Json.obj(
        "_t" -> "a",
        "0" -> Json.arr(3),
        "4" -> Json.arr(2),
        "8" -> Json.arr(1)
      )
    )
    diffJson(s1, s2) shouldBe expected
  }

  it should "handle object in array diff" in {
    val s1 = """{"1": [{"1":1}]}"""
    val s2 = """{"1": [{"1":2}]}"""
    val expected = Json.obj(
      "1" -> Json.obj(
        "0" -> Json.obj("1" -> Json.arr(1, 2)),
        "_t" -> "a"
      )
    )
    diffJson(s1, s2) shouldBe expected
  }

  it should "handle object with multiple values in array diff" in {
    val s1 = """{"1": [{"1":1,"2":2}]}"""
    val s2 = """{"1": [{"1":2,"2":2}]}"""
    val expected = Json.obj(
      "1" -> Json.obj(
        "0" -> Json.obj("1" -> Json.arr(1, 2)),
        "_t" -> "a"
      )
    )
    diffJson(s1, s2) shouldBe expected
  }

  it should "handle object with multiple values plus in array diff" in {
    val s1 = """{"1": [{"1":1,"2":2},{"3":3,"4":4}]}"""
    val s2 = """{"1": [{"1":2,"2":2},{"3":5,"4":6}]}"""
    val expected = Json.obj(
      "1" -> Json.obj(
        "0" -> Json.obj("1" -> Json.arr(1, 2)),
        "1" -> Json.obj("3" -> Json.arr(3, 5), "4" -> Json.arr(4, 6)),
        "_t" -> "a"
      )
    )
    diffJson(s1, s2) shouldBe expected
  }

  it should "handle one object in array diff" in {
    val s1 = """{"1": [1]}"""
    val s2 = """{"1": [{"1":2}]}"""
    val expected = Json.obj(
      "1" -> Json.obj(
        "0" -> Json.arr(Json.obj("1" -> 2)),
        "_0" -> Json.arr(1, 0, 0),
        "_t" -> "a"
      )
    )
    diffJson(s1, s2) shouldBe expected
  }

  it should "handle deleted value with object with change in array diff" in {
    val s1 = """{"1": [1,{"1":1}]}"""
    val s2 = """{"1": [{"1":2}]}"""
    val expected = Json.obj(
      "1" -> Json.obj(
        "0" -> Json.arr(Json.obj("1" -> 2)),
        "_0" -> Json.arr(1, 0, 0),
        "_1" -> Json.arr(Json.obj("1" -> 1), 0, 0),
        "_t" -> "a"
      )
    )
    diffJson(s1, s2) shouldBe expected
  }

  it should "handle same numeric type" in {
    val j1 = Json.obj("1" -> 4, "2" -> 2)
    val j2 = Json.obj("1" -> 4, "2" -> 2)
    new JsonDiff(j1, j2).diff() shouldBe Json.obj()
  }
}
