// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: 2021 The Elixir Team
// SPDX-FileCopyrightText: 2012 Plataformatec
// SPDX-FileCopyrightText: 2025 Olafur Arason
// From https://github.com/elixir-lang/elixir/blob/main/lib/elixir/lib/list.ex

package com.olafura.jsondiff

import scala.annotation.tailrec 
import scala.collection.immutable.IndexedSeq
import scala.collection.immutable.List


case class Equals(value: List[Any])
case class Insert(value: List[Any])
case class Delete(value: List[Any])

type Diff = Equals | Insert | Delete
type DiffList = List[Diff] 

case class Done(value: DiffList)
case class Path(index: Int, oldSeq: List[Any], newSeq: List[Any], edits: DiffList)

type PathList = List[Path] 

case class ProcessedPath(path: Option[Path], rest: PathList)

case class Next(value: PathList)
case class Continue(value: Path)

class Myers(oldSeq: IndexedSeq[Any], newSeq: IndexedSeq[Any]):
  def diff(): DiffList =
    val path = Path(0, List.from(oldSeq), List.from(newSeq), List())
    find(0, oldSeq.length + newSeq.length, List(path))

  @tailrec
  private def find(envelope: Int, max: Int, paths: PathList): DiffList =
    eachDiagonal(-envelope, envelope, paths, List()) match
      case Done(edits) =>
        compactReverse(edits, List())
      case Next(paths) =>
        find(envelope + 1, max, paths)

  @tailrec
  private def compactReverse(edits: DiffList, acc: DiffList): DiffList = (edits, acc) match
    case (Nil, _) =>
      acc
    case (Equals(elem) :: rest, Equals(result) :: accRest) =>
      compactReverse(rest, Equals(elem ::: result) :: accRest)
    case (Insert(elem) :: rest, Insert(result) :: accRest) =>
      compactReverse(rest, Insert(elem ::: result) :: accRest)
    case (Delete(elem) :: rest, Delete(result) :: accRest) =>
      compactReverse(rest, Delete(elem ::: result) :: accRest)
    case (rest, Equals(elem1) :: Insert(elem2) :: Equals(other) :: accRest) if elem1 == elem2 =>
      compactReverse(rest, Insert(elem1) :: Equals(elem1 ::: other) :: accRest)
    case (Equals(elem) :: rest, accRest) =>
      compactReverse(rest, Equals(elem) :: accRest)
    case (Insert(elem) :: rest, accRest) =>
      compactReverse(rest, Insert(elem) :: accRest)
    case (Delete(elem) :: rest, accRest) =>
      compactReverse(rest, Delete(elem) :: accRest)

  @tailrec
  private def eachDiagonal(diagonal: Int, limit: Int, paths: PathList, nextPaths: PathList): Done | Next = (diagonal, limit) match
    case (diagonal, limit) if diagonal > limit =>
      Next(nextPaths.reverse)
    case _ =>
      val processedPath = processPath(diagonal, limit, paths)

      followSnake(processedPath.path) match
        case Continue(path) =>
          eachDiagonal(diagonal + 2, limit, processedPath.rest, path :: nextPaths)
        case Done(edits) =>
          Done(edits)

  private def processPath(diagonal: Int, limit: Int, paths: PathList): ProcessedPath = (diagonal, limit, paths) match
    case (0, 0, path :: Nil) =>
      ProcessedPath(Some(path), List())
    case (0, 0, Nil) =>
      ProcessedPath(None, List())
    case (diagonal, limit, path :: _) if diagonal == -limit =>
      ProcessedPath(Some(moveDown(path)), paths)
    case (diagonal, limit, path :: Nil) if diagonal == limit =>
      ProcessedPath(Some(moveRight(path)), List())
    case (_, _, path1 :: path2 :: rest) if path1.index > path2.index =>
      ProcessedPath(Some(moveRight(path1)), path2 :: rest)
    case (_, _, path1 :: path2 :: rest) if path1.index <= path2.index =>
      ProcessedPath(Some(moveDown(path2)), path2 :: rest)
    case (_, _, _) =>
      ProcessedPath(None, List())

  private def moveRight(path: Path): Path = path match
    case Path(index, list1, elem :: Nil, edits) =>
      Path(index, list1, List(), Insert(List(elem)) :: edits)
    case Path(index, list1, elem :: rest, edits) =>
      Path(index, list1, rest, Insert(List(elem)) :: edits)
    case Path(index, list1, Nil, edits) =>
      Path(index, list1, List(), edits)

  private def moveDown(path: Path): Path = path match
    case Path(index, elem :: rest, list2, edits) =>
      Path(index + 1, rest, list2, Delete(List(elem)) :: edits)
    case Path(index, Nil, list2, edits) =>
      Path(index + 1, List(), list2, edits)

  @tailrec
  private def followSnake(path: Option[Path]): Done | Continue = path match
    case Some(Path(index, elem1 :: rest1, elem2 :: rest2, edits)) if elem1 == elem2 =>
      followSnake(Some(Path(index + 1, rest1, rest2, Equals(List(elem1)) :: edits)))
    case Some(Path(_, Nil, Nil, edits)) =>
      Done(edits)
    case Some(other) =>
      Continue(other)
    case None =>
      Done(List())
