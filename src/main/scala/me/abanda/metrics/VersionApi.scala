package me.abanda.metrics

import cats.effect.IO
import me.abanda.http.Http
import me.abanda.infrastructure.Json._
import me.abanda.version.BuildInfo
import sttp.tapir.server.ServerEndpoint

/** Defines an endpoint which exposes the current application version information.
  */
class VersionApi extends Http {
  import VersionApi._

  val versionEndpoint: ServerEndpoint[Any, IO] = baseEndpoint.get
    .in("version")
    .out(jsonBody[Version_OUT])
    .serverLogic { _ =>
      IO(Version_OUT(BuildInfo.gitCommit)).toOut
    }
}

object VersionApi {
  case class Version_OUT(buildSha: String)
}
