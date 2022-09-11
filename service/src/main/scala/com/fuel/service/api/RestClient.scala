package com.fuel.service.api

import org.http4s.client._
import cats.effect._
import org.http4s._
import org.http4s.dsl.io.POST
import java.time.LocalDate
import org.typelevel.ci.CIString
import org.http4s.client.dsl.io._

trait RestClient[F[_]] {
  def sendRequest(date: LocalDate)(onError: Response[F] => F[Throwable]): F[Double]
}

class RestClientImp(url: String, apiKey: String) extends RestClient[IO] {

  import JsonImplicits._

  val httpClient: Client[IO] = JavaNetClientBuilder[IO].create

  override def sendRequest(date: LocalDate)(onError: Response[IO] => IO[Throwable]): IO[Double] = {
    val request = POST(
      date,
      Uri.unsafeFromString(url),
      Headers(
        Header.Raw(CIString("x-api-key"), apiKey),
        Header.Raw(CIString("Content-Type"), "application/json")
      )
    )
    httpClient.expect[Double](request)
  }

}
