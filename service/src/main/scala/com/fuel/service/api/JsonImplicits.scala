package com.fuel.service.api

import cats.effect.IO
import com.fuel.domain.{ApiRequest, ApiResponse, ConsumptionAndEmission, RoutePoint}
import io.circe.Decoder.Result
import io.circe.{Decoder, DecodingFailure, Encoder, HCursor, Json, JsonObject}
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import io.circe.syntax._
import java.time.LocalDate

object JsonImplicits {

  def decodeDouble(o: Option[JsonObject], key: String): Result[Double] =
      o.flatMap(_ (key)).flatMap(_.asNumber) match {
      case Some(value) => Right(value.toDouble)
      case None => Left(DecodingFailure("failed to decode json", Nil))
    }

  implicit val jsonDecoder: Decoder[Double] = new Decoder[Double] {
    override def apply(c: HCursor): Result[Double] = {
      decodeDouble(c.value.asObject, "Beaufort")
    }
  }

  implicit val authResponseEntityDecoder: EntityDecoder[IO, Double] = jsonOf[IO, Double]

  implicit val dateEncoder: Encoder[LocalDate] = new Encoder[LocalDate] {
    override def apply(d: LocalDate): Json =
      Json.obj("Date" -> Json.fromString(d.toString))
  }

  implicit val encoder: EntityEncoder[IO, LocalDate] = jsonEncoderOf[IO, LocalDate]

  implicit val routePointDecoder: Decoder[RoutePoint] = new Decoder[RoutePoint] {
    override def apply(c: HCursor): Result[RoutePoint] = {
      (for {
        date <- c.downField("date").as[String]
        longitude <- decodeDouble(c.value.asObject, "longitude")
        latitude <- decodeDouble(c.value.asObject, "latitude")
      } yield RoutePoint(date, longitude, latitude)).flatMap {
        case Right(value) => Right(value)
        case Left(e) => Left(DecodingFailure.fromThrowable(e, Nil))
      }
    }
  }

  implicit val routePointEntityDecoder: EntityDecoder[IO, RoutePoint] = jsonOf[IO, RoutePoint]

  implicit val authResponseDecoder: Decoder[ApiRequest] = new Decoder[ApiRequest] {
    override def apply(c: HCursor): Result[ApiRequest] = {
      for {
        imo <- c.downField("imo").as[Int]
        draught <- decodeDouble(c.value.asObject, "draught")
        routes <- c.downField("routes").as[List[List[RoutePoint]]]
        eco <- c.downField("eco").as[Option[Boolean]]
      } yield ApiRequest(imo, draught, routes, eco.getOrElse(false))
    }
  }

  implicit val apiRequestEntityDecoder: EntityDecoder[IO, ApiRequest] = jsonOf[IO, ApiRequest]

  implicit val consumptionAndEmissionDecoder: Encoder[ConsumptionAndEmission] = new Encoder[ConsumptionAndEmission] {
    override def apply(a: ConsumptionAndEmission): Json = Json.obj(
      "average_consumption" -> Json.fromDoubleOrNull(a.consumption),
      "average_emission" -> Json.fromDoubleOrNull(a.emission)
    )
  }

  implicit val apiResponseEncoder: Encoder[ApiResponse] = new Encoder[ApiResponse] {
    def baseObject(a: ApiResponse): Json =
      Json.obj("response" -> Json.arr(a.averageConsumptionPerRoute.map(_.asJson): _*))

    override def apply(a: ApiResponse): Json = {
      a.eco match {
        case Some(v) => baseObject(a).deepMerge(Json.obj("route_with_lowest_emission" -> v.asJson))
        case None => baseObject(a)
      }
    }
  }

  implicit val apiResponseEntityDecoder: EntityEncoder[IO, ApiResponse] = jsonEncoderOf[IO, ApiResponse]

  implicit val errorResponseEncoder: Encoder[Throwable] = new Encoder[Throwable] {
    override def apply(e: Throwable): Json = Json.obj("error" -> Json.fromString(e.getMessage))
  }

  implicit val errorResponseEntityEncoder: EntityEncoder[IO, Throwable] = jsonEncoderOf[IO, Throwable]

}
