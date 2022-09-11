package com.fuel.service.api

import cats.data.Kleisli
import cats.effect._
import com.fuel.domain.{ApiRequest, ApiResponse}
import org.http4s._
import org.http4s.dsl.io._

class RestEndpoint(f: ApiRequest => IO[Either[Throwable, ApiResponse]]) {

  import com.fuel.service.api.JsonImplicits._

  lazy val service: Kleisli[IO, Request[IO], Response[IO]] = HttpRoutes.of[IO] {
    case req@POST -> Root / "estimate-consumption" =>
      for {
        request <- req.as[ApiRequest]
        apiResponse <- f(request)
        response <- apiResponse match {
          case Right(value) => Ok(value)
          case Left(e) => BadRequest(e)
        }
      } yield response
  }.orNotFound

}
