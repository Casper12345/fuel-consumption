package com.fuel.service.service

import com.fuel.domain.{ConsumptionAndEmission, RoutePoint}
import java.time.{LocalDate, ZonedDateTime}
import java.time.temporal.ChronoUnit
import scala.annotation.tailrec

object CalculatorLogic {

  private def calculateAverageSpeedPerDay(xs: List[RoutePoint]): Option[Double] = {
    // knots 1 nautical mile per hour
    @tailrec
    def go(i: Int, acc: Double): Double = {
      if (i > xs.length - 2) acc else {
        val rp1: RoutePoint = xs(i)
        val rp2 = xs(i + 1)
        val distance = haversineDistance((rp1.longitude, rp1.latitude), (rp2.longitude, rp2.latitude))
        val delta = calculateDelta(rp1.date, rp2.date)
        val speed =  if(delta == 0D) 0.0 else distance / delta
        go(i + 1, acc + speed)
      }
    }

    if (xs.length > 1) Some(go(0, 0.0) / xs.length.toDouble) else None
  }


  private def haversineDistance(d1: (Double, Double), d2: (Double, Double)): Double = {
    val lon1 = Math.toRadians(d1._1)
    val lon2 = Math.toRadians(d2._1)
    val lat1 = Math.toRadians(d1._2)
    val lat2 = Math.toRadians(d2._2)

    val dLon = lon2 - lon1
    val dLat = lat2 - lat1

    val a = Math.pow(Math.sin(dLat / 2), 2) +
      Math.cos(lat1) *
        Math.cos(lat2) *
        Math.pow(Math.sin(dLon / 2), 2)

    val c = 2 * Math.asin(Math.sqrt(a))
    val r = 6371 // radius in kilometers
    val toNauticalMile = 1.852

    (c * r) / toNauticalMile
  }

  private def calculateDelta(t1: ZonedDateTime, t2: ZonedDateTime): Double =
    ChronoUnit.SECONDS.between(t1, t2) / 60D / 60D

  private def groupByDate(xs: List[RoutePoint]): List[(List[RoutePoint], LocalDate)] = {
    xs.groupBy(_.date.toLocalDate).map { case (d, rp) => (rp, d) }.toList
  }

  def getSpeedPerRoute(xs: List[RoutePoint]): List[(LocalDate, Double)] =
    groupByDate(xs.distinctBy(_.date.toLocalDateTime))
      .flatMap { case (rp, d) => calculateAverageSpeedPerDay(rp).map((d,_)) }

  def calculateAverageConsumptionAndEmissionPerRoute(xs: List[List[Double]]): List[ConsumptionAndEmission] = {
    val EmissionConst = 3.114
    xs.map(xs => xs.sum / xs.length.toDouble).zipWithIndex.map {
      case (d,i)  => ConsumptionAndEmission(i+1, d, d * EmissionConst)
    }
  }

  def getLowestEmissionRoute(xs: List[ConsumptionAndEmission]): Option[Int] =
    xs.sortBy(_.emission).headOption.map(_.routeNumber)

}
