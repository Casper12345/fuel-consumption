package com.fuel.service.api

import cats.effect.IO
import org.http4s.Uri
import cats.effect.unsafe.implicits.global
import org.http4s.dsl.io.POST
import org.scalatest.flatspec.AsyncFlatSpec
import io.circe.{Encoder, Json}
import org.http4s._
import org.http4s.circe.jsonEncoderOf
import org.http4s.client.dsl.io._
import org.scalatest.matchers.should.Matchers
import java.time.ZonedDateTime
import com.fuel.domain.{ApiRequest, ApiResponse, ConsumptionAndEmission, RoutePoint}
import Implicits._

class RestEndpointTest extends AsyncFlatSpec with Matchers {

  "service" should "return 200 on correct request" in {

    def fun(a: ApiRequest): IO[Either[Throwable, ApiResponse]] =
      IO.pure(Right(ApiResponse(List.empty[ConsumptionAndEmission], None)))

    val restEndpoint = new RestEndpoint(fun)

    val routePoints = List(
      List(
        RoutePoint(ZonedDateTime.parse("2022-03-02T07:21:00Z"), -35.89555555555555, -16.020833333333332)
      )
    )

    val body = ApiRequest(123, 3.0, routePoints, eco = false)

    val request = POST(
      body,
      Uri.unsafeFromString("/estimate-consumption")
    )

    for {
      response <- restEndpoint.service.run(request).unsafeToFuture()
      body <- response.bodyText.compile.toList.unsafeToFuture()
    } yield {
      body.head shouldEqual """{"response":[]}"""
      response.status.code shouldEqual 200
    }

  }

  it should "return 400 error" in {

    def fun(a: ApiRequest): IO[Either[Throwable, ApiResponse]] =
      IO.pure(Left(new Exception("something bad happened")))

    val restEndpoint = new RestEndpoint(fun)

    val routePoints = List(
      List(
        RoutePoint(ZonedDateTime.parse("2022-03-02T07:21:00Z"), -35.89555555555555, -16.020833333333332)
      )
    )

    val body = ApiRequest(123, 3.0, routePoints, eco = false)

    val request = POST(
      body,
      Uri.unsafeFromString("/estimate-consumption")
    )

    for {
      response <- restEndpoint.service.run(request).unsafeToFuture()
      body <- response.bodyText.compile.toList.unsafeToFuture()
    } yield {
      body.head shouldEqual """{"error":"something bad happened"}"""
      response.status.code shouldEqual 400
    }

  }

}
object Implicits {

  implicit val requestEncoder: Encoder[ApiRequest] = new Encoder[ApiRequest] {
    override def apply(d: ApiRequest): Json = {
      Json.obj(
        "imo" -> Json.fromInt(123),
        "draught" -> Json.fromDoubleOrNull(3.0D),
        "routes" -> Json.arr(
          Json.arr(
            Json.obj(
              "date" -> Json.fromString("2022-03-02T07:21:00Z"),
              "longitude" -> Json.fromDoubleOrNull(-35.89555555555555),
              "latitude" -> Json.fromDoubleOrNull(-16.020833333333332)
            )
          )
        )
      )
    }
  }

  implicit val apiRequestEntityDecoder: EntityEncoder[IO, ApiRequest] = jsonEncoderOf[IO, ApiRequest]

}
