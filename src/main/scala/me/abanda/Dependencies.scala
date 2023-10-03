package me.abanda

import cats.effect.{IO, Resource}
import com.softwaremill.macwire.autocats.autowire
import doobie.util.transactor.Transactor
import io.prometheus.client.CollectorRegistry
import me.abanda.config.Config
import me.abanda.http.{Http, HttpApi, HttpConfig}
import me.abanda.metrics.VersionApi
import me.abanda.util.Clock
import sttp.client3.SttpBackend
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics

case class Dependencies(httpApi: HttpApi)

object Dependencies {
  def wire(
      config: Config,
//      sttpBackend: Resource[IO, SttpBackend[IO, Any]],
//      xa: Resource[IO, Transactor[IO]],
//      clock: Clock,
      collectorRegistry: CollectorRegistry
  ): Resource[IO, Dependencies] = {
    def buildHttpApi(
        versionApi: VersionApi,
        cfg: HttpConfig
    ) = {
      val prometheusMetrics = PrometheusMetrics.default[IO](registry = collectorRegistry)
      new HttpApi(
        List.empty,
        List(versionApi.versionEndpoint),
        prometheusMetrics,
        cfg
      )
    }

    autowire[Dependencies](
      config.api,

//      DefaultIdGenerator,
//      clock,
//      sttpBackend,
//      xa,
      buildHttpApi _
    )
  }
}
