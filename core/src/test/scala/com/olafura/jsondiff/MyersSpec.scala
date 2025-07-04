package com.olafura.jsondiff

import org.scalatest._
import flatspec._

class MyersSpec extends AnyFlatSpec {

  private def diff[A](oldSeq: Seq[A], newSeq: Seq[A]) =
    new Myers(oldSeq.toIndexedSeq, newSeq.toIndexedSeq).diff()

  it should "follows paper implementation" in {
    assert(diff(Seq.empty, Seq.empty) == List())
    assert(diff(Seq.empty, Seq(1, 2, 3)) == List(Insert(List(1, 2, 3))))
    assert(diff(Seq(1, 2, 3), Seq.empty) == List(Delete(List(1, 2, 3))))
    assert(diff(Seq(1, 2, 3), Seq(1, 2, 3)) == List(Equals(List(1, 2, 3))))
    assert(
      diff(Seq(1, 2, 3), Seq(1, 4, 2, 3)) ==
        List(Equals(List(1)), Insert(List(4)), Equals(List(2, 3)))
    )
    assert(
      diff(Seq(1, 4, 2, 3), Seq(1, 2, 3)) ==
        List(Equals(List(1)), Delete(List(4)), Equals(List(2, 3)))
    )
    assert(
      diff(Seq(1), Seq(List(1))) == List(Delete(List(1)), Insert(List(List(1))))
    )
    assert(
      diff(Seq(List(1)), Seq(1)) == List(Delete(List(List(1))), Insert(List(1)))
    )
  }

  it should "rearranges inserts and equals for smaller diffs" in {
    assert(
      diff(Seq(3, 2, 0, 2), Seq(2, 2, 0, 2)) ==
        List(Delete(List(3)), Insert(List(2)), Equals(List(2, 0, 2)))
    )
    assert(
      diff(Seq(3, 2, 1, 0, 2), Seq(2, 1, 2, 1, 0, 2)) ==
        List(Delete(List(3)), Insert(List(2, 1)), Equals(List(2, 1, 0, 2)))
    )
    assert(
      diff(Seq(3, 2, 2, 1, 0, 2), Seq(2, 2, 1, 2, 1, 0, 2)) ==
        List(
          Delete(List(3)),
          Equals(List(2, 2, 1)),
          Insert(List(2, 1)),
          Equals(List(0, 2))
        )
    )
    assert(
      diff(Seq(3, 2, 0, 2), Seq(2, 2, 1, 0, 2)) ==
        List(
          Delete(List(3)),
          Equals(List(2)),
          Insert(List(2, 1)),
          Equals(List(0, 2))
        )
    )
  }
}
