package me.abanda.test

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.circe.generic.auto.exportDecoder
import io.circe.{Decoder, parser}
import me.abanda.http.Error_OUT

import scala.concurrent.duration.DurationInt
import scala.reflect.ClassTag

trait TestSupport {

  implicit class RichEiter(r: Either[String, String]) {
    def shouldDeserializeTo[T: Decoder: ClassTag]: T =
      r.flatMap(parser.parse).flatMap(_.as[T]).right.get

    def shouldDeserializeToError: String = {
      parser.parse(r.left.get).flatMap(_.as[Error_OUT]).right.get.error
    }
  }

  implicit class RichIO[T](t: IO[T]) {
    def unwrap: T = t.unsafeRunTimed(1.minute).get
  }
}
