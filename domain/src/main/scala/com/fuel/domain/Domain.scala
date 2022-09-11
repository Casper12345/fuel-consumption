package com.fuel.domain

import java.time.ZonedDateTime
import scala.util.Try

case class RoutePoint(date: ZonedDateTime, longitude: Double, latitude: Double)

object RoutePoint {

  private def parseDate(s: String): Either[Throwable, ZonedDateTime] =
    Try(ZonedDateTime.parse(s)).toEither

  def apply(date: String, longitude: Double, latitude: Double): Either[Throwable, RoutePoint] =
    parseDate(date).map(dt => RoutePoint(dt, longitude, latitude))
}

sealed trait FuelConsumptionData

case class FuelConsumptionDataType1(
  imo: Int,
  draught: Double,
  speed: Double,
  beaufort: Double,
  consumption: Double
) extends FuelConsumptionData

case class FuelConsumptionDataType2(
  imo: Int,
  draught: Double,
  speed: Double,
  beaufort: Double,
  consumption: Double,
  addedResistance: Int
) extends FuelConsumptionData

case class FuelConsumptionDataType3(
 imo: Int,
 draught: Double,
 speed: Double,
 beaufort: Double,
 consumptionMainEngineMtPerDay: Double,
 consumptionAuxiliaryEngineMtPerDay: Double,
 consumptionBoilerEngineMtPerDay: Double
) extends FuelConsumptionData

case class FuelConsumptionDataType4(
 imo: Int,
 draught: Double,
 speed: Double,
 beaufort: Double,
 consumption: Double,
 trim: Double
) extends FuelConsumptionData

case class TableQuery(
 imo: Int,
 draught: Double,
 speed: Double,
 beaufort: Double
)

case class ApiRequest(imo: Int, draught: Double, routes: List[List[RoutePoint]], eco: Boolean)

case class ConsumptionAndEmission(routeNumber: Int, consumption: Double, emission: Double)

case class ApiResponse(averageConsumptionPerRoute: List[ConsumptionAndEmission], eco: Option[Int])