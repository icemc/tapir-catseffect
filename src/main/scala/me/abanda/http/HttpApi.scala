package me.abanda.http

import cats.effect.{IO, Resource}
import me.abanda.infrastructure.CorrelationIdInterceptor
import me.abanda.logging.FLogger
import me.abanda.util.ServerEndpoints
import com.typesafe.scalalogging.StrictLogging
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import sttp.tapir.{EndpointInput, emptyInput}
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}
import sttp.tapir.server.interceptor.cors.CORSInterceptor
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import sttp.tapir.server.model.ValuedEndpointOutput
import sttp.tapir.swagger.SwaggerUIOptions
import sttp.tapir.swagger.bundle.SwaggerInterpreter

/** Interprets the endpoint descriptions (defined using tapir) as http4s routes, adding CORS, metrics, api docs support.
  *
  * The following endpoints are exposed:
  *   - `/api/v1` - the main API
  *   - `/api/v1/docs` - swagger UI for the main API
  *   - `/admin` - admin API
  *   - `/` - serving frontend resources
  */
class HttpApi(
    http: Http,
    mainEndpoints: ServerEndpoints,
    adminEndpoints: ServerEndpoints,
    prometheusMetrics: PrometheusMetrics[IO],
    config: HttpConfig
) extends StrictLogging {
  private val apiContextPath = List("api", "v1")

  val serverOptions: Http4sServerOptions[IO] = Http4sServerOptions
    .customiseInterceptors[IO]
    .prependInterceptor(CorrelationIdInterceptor)
    // all errors are formatted as json, and there are no other additional http4s routes
    .defaultHandlers(msg => ValuedEndpointOutput(http.jsonErrorOutOutput, Error_OUT(msg)), notFoundWhenRejected = true)
    .serverLog {
      // using a context-aware logger for http logging
      val flogger = new FLogger(logger)
      Http4sServerOptions
        .defaultServerLog[IO]
        .doLogWhenHandled((msg, e) => e.fold(flogger.debug[IO](msg))(flogger.debug(msg, _)))
        .doLogAllDecodeFailures((msg, e) => e.fold(flogger.debug[IO](msg))(flogger.debug(msg, _)))
        .doLogExceptions((msg, e) => flogger.error[IO](msg, e))
        .doLogWhenReceived(msg => flogger.debug[IO](msg))
    }
    .corsInterceptor(CORSInterceptor.default[IO])
    .metricsInterceptor(prometheusMetrics.metricsInterceptor())
    .options

  lazy val routes: HttpRoutes[IO] = Http4sServerInterpreter(serverOptions).toRoutes(allEndpoints)

  lazy val allEndpoints: List[ServerEndpoint[Any, IO]] = {
    // creating the documentation using `mainEndpoints` without the /api/v1 context path; instead, a server will be added
    // with the appropriate suffix
    val docsEndpoints = SwaggerInterpreter(swaggerUIOptions = SwaggerUIOptions.default.copy(contextPath = apiContextPath))
      .fromServerEndpoints(mainEndpoints.toList, "REST API", "1.0")

    // for /api/v1 requests, first trying the API; then the docs
    val apiEndpoints =
      (mainEndpoints ++ docsEndpoints).map(se => se.prependSecurityIn(apiContextPath.foldLeft(emptyInput: EndpointInput[Unit])(_ / _)))

    val allAdminEndpoints = (adminEndpoints ++ List(prometheusMetrics.metricsEndpoint)).map(_.prependSecurityIn("admin"))

    apiEndpoints.toList ++ allAdminEndpoints.toList
  }

  /** The resource describing the HTTP server; binds when the resource is allocated. */
  lazy val resource: Resource[IO, org.http4s.server.Server] = BlazeServerBuilder[IO]
    .bindHttp(config.port, config.host)
    .withHttpApp(routes.orNotFound)
    .resource
}
