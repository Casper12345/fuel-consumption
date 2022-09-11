package com.fuel.service.init

import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s.{Ipv4Address, Port}
import com.fuel.dao.{Connector, FuelConsumptionDaoImpl}
import com.fuel.service.api.{RestClientImp, RestEndpoint}
import com.fuel.service.service.FuelConsumptionCalculatorImpl
import com.typesafe.config.ConfigFactory
import org.http4s.ember.server.EmberServerBuilder

object Main extends IOApp {

  val config = ConfigFactory.load(this.getClass.getClassLoader)

  val fuelConsumptionDao = new FuelConsumptionDaoImpl(Connector.xa)

  val restClient = new RestClientImp(
    config.getString("rest-api.client.url"),
    config.getString("rest-api.client.api-key")
  )

  val fuelConsumptionCalculator = new FuelConsumptionCalculatorImpl(fuelConsumptionDao, restClient)

  val restEndpoint = new RestEndpoint(fuelConsumptionCalculator.calculateFuelConsumption)

  override def run(args: List[String]): IO[ExitCode] = {

    (for  {
      host <- Ipv4Address.fromString(config.getString("rest-api.endpoint.host"))
      port <- Port.fromInt(config.getInt("rest-api.endpoint.port"))
    } yield {
      EmberServerBuilder
        .default[IO]
        .withHost(host)
        .withPort(port)
        .withHttpApp(restEndpoint.service)
        .build
        .use(_ => IO.never)
        .as(ExitCode.Success)
    }).getOrElse(IO.pure(ExitCode.Error))
  }
}
