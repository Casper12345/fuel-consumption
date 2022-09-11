package com.fuel.service.service

import cats.effect.IO
import org.http4s.Response
import cats.implicits._
import com.fuel.dao.FuelConsumptionDao
import com.fuel.domain.{ApiRequest, ApiResponse, TableQuery}
import com.fuel.service.api.RestClient
import com.fuel.util.cache.DateCache
import java.time.LocalDate

trait FuelConsumptionCalculator[F[_]] {
  def calculateFuelConsumption(r: ApiRequest): F[Either[Throwable, ApiResponse]]
}

class FuelConsumptionCalculatorImpl(
 dao: FuelConsumptionDao[IO],
 restClient: RestClient[IO]
) extends FuelConsumptionCalculator[IO] {

  override def calculateFuelConsumption(r: ApiRequest): IO[Either[Throwable, ApiResponse]] = {
    val dateCache = DateCache
    val ApiRequest(imo, draught, routes, eco) = r

    def onError(r: Response[IO]): IO[Throwable] =
      IO.pure(new Exception(s"api call failed with status: ${r.status}"))

    val speedPerRoute: List[List[(LocalDate, Double)]] = routes.map(CalculatorLogic.getSpeedPerRoute)

    for {
      tableQueries <- speedPerRoute
        .map(_.map { case (d, s) => dateCache.getOrCache(d)(restClient.sendRequest(_)(onError))
          .map(TableQuery(imo, draught, s, _)) }
          .sequence
        ).sequence
      fuelConsumptionPerRoute <-
        dao.getFuelConsumption(tableQueries)
    } yield {

      val averageConsumptionPerRoute =
        CalculatorLogic.calculateAverageConsumptionAndEmissionPerRoute(fuelConsumptionPerRoute)

      if(eco){
        Right(ApiResponse(averageConsumptionPerRoute, CalculatorLogic.getLowestEmissionRoute(averageConsumptionPerRoute)))
      } else {
        Right(ApiResponse(averageConsumptionPerRoute, None))
      }
    }

  }
}
