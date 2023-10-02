package me.abanda

import cats.data.NonEmptyList
import cats.effect.{IO, Resource}
import com.softwaremill.macwire.autocats.autowire
import doobie.util.transactor.Transactor
import io.prometheus.client.CollectorRegistry
import me.abanda.config.Config
import me.abanda.email.EmailService
import me.abanda.email.sender.EmailSender
import me.abanda.http.{Http, HttpApi, HttpConfig}
import me.abanda.metrics.VersionApi
import me.abanda.passwordreset.{PasswordResetApi, PasswordResetAuthToken}
import me.abanda.security.ApiKeyAuthToken
import me.abanda.user.UserApi
import me.abanda.util.{Clock, DefaultIdGenerator}
import sttp.client3.SttpBackend
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics

case class Dependencies(httpApi: HttpApi, emailService: EmailService)

object Dependencies {
  def wire(
      config: Config,
      sttpBackend: Resource[IO, SttpBackend[IO, Any]],
      xa: Resource[IO, Transactor[IO]],
      clock: Clock,
      collectorRegistry: CollectorRegistry
  ): Resource[IO, Dependencies] = {
    def buildHttpApi(
        http: Http,
        userApi: UserApi,
        passwordResetApi: PasswordResetApi,
        versionApi: VersionApi,
        cfg: HttpConfig
    ) = {
      val prometheusMetrics = PrometheusMetrics.default[IO](registry = collectorRegistry)
      new HttpApi(
        http,
        userApi.endpoints concatNel passwordResetApi.endpoints,
        NonEmptyList.of(versionApi.versionEndpoint),
        prometheusMetrics,
        cfg
      )
    }

    autowire[Dependencies](
      config.api,
      config.user,
      config.passwordReset,
      config.email,
      DefaultIdGenerator,
      clock,
      sttpBackend,
      xa,
      buildHttpApi _,
      new EmailService(_, _, _, _, _),
      EmailSender.create _,
      new ApiKeyAuthToken(_),
      new PasswordResetAuthToken(_)
    )
  }
}
