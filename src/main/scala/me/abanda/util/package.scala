package me.abanda

import cats.effect.IO
import com.softwaremill.tagging._
import sttp.tapir.server.ServerEndpoint

package object util {
  type SecureRandomId <: String
  type Id = SecureRandomId

  implicit class RichString(val s: String) extends AnyVal {
    def asId[T]: Id @@ T = s.asInstanceOf[Id @@ T]
  }

  type ServerEndpoints = List[ServerEndpoint[Any, IO]]
}
