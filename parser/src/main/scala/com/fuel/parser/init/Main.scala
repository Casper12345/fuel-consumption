package com.fuel.parser.init

import cats.effect.{ExitCode, IO, IOApp}
import scala.jdk.CollectionConverters._
import java.nio.file.{Files, Path, Paths}
import cats.implicits._
import com.fuel.dao.{Connector, FuelConsumptionDaoImpl}
import com.fuel.domain.{FuelConsumptionData, FuelConsumptionDataType1, FuelConsumptionDataType2, FuelConsumptionDataType3, FuelConsumptionDataType4}
import com.fuel.parser.parser.CsvParser
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Main extends IOApp {

  private val logger = LoggerFactory.getLogger(getClass)

  private val config = ConfigFactory.load(getClass.getClassLoader)

  private val fuelConsumptionDao: FuelConsumptionDaoImpl = new FuelConsumptionDaoImpl(Connector.xa)

  private val csvFiles: List[Path] = Files.list(Paths.get(config.getString("csv-parser.file-path")))
    .toList.asScala.toList.filter(_.toString.endsWith("csv"))

  private val parallelism: Int = config.getInt("csv-parser.parallelism")

  override def run(args: List[String]): IO[ExitCode] =
    for {
      parsedFiles <- csvFiles.map(CsvParser.parseFile(_)(parallelism)).sequence
      errors = parsedFiles.map(_.collect { case Left(e) => e }).flatMap(_.map(_.getMessage))
      xs = parsedFiles.flatMap(_.collect[FuelConsumptionData] {
        case Right(v: FuelConsumptionDataType1) => v
        case Right(v: FuelConsumptionDataType2) => v
        case Right(v: FuelConsumptionDataType3) => v
        case Right(v: FuelConsumptionDataType4) => v
      })
      _ <- fuelConsumptionDao.bulkInsertFuelConsumptionData(xs)
    } yield {
      errors.foreach(logger.info)
      ExitCode.Success
    }

}
