package me.abanda.test

import cats.effect.IO
import sttp.client3
import sttp.client3.{SttpBackend, UriContext, basicRequest}

import scala.util.Random

class Requests(backend: SttpBackend[IO, Any]) extends TestSupport {

  private val random = new Random()

  def randomLoginEmailPassword(): (String, String, String) =
    (random.nextString(12), s"user${random.nextInt(9000)}@bootzooka.com", random.nextString(12))

  private val basePath = "http://localhost:8080/api/v1"


  //All request to stub backend should be added here

  def getMetrics: client3.Response[Either[String, String]] = {
    basicRequest
      .get(uri"$basePath/metrics")
      .send(backend)
      .unwrap
  }

}
