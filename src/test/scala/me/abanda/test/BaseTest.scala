package me.abanda.test

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

trait BaseTest extends AnyFlatSpec with Matchers {
  val testClock = new TestClock()
}
