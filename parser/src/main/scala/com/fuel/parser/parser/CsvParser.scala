package com.fuel.parser.parser

import cats.effect._
import com.fuel.domain.{FuelConsumptionData, FuelConsumptionDataType1, FuelConsumptionDataType2, FuelConsumptionDataType3, FuelConsumptionDataType4}
import fs2._
import fs2.io.file.{Files, Path}
import scala.util.Try
import java.nio.file.{Path => JPath}

object CsvParser {

  private def parser[F[_]]: Pipe[F, Byte, List[String]] =
     _.through(text.utf8.decode)
      .through(text.lines)
      .map(_.split(',').toList)

  private def getParser(xs: List[String]): List[String] => Either[Throwable, FuelConsumptionData] = {

    def predicateType1(s1: String, s2: String, s3: String, s4: String, s5: String): Boolean =
      s1 == "draught" && s2 == "speed" && s3 == "beaufort" && s4 == "consumption" && s5 == "imo"

    def predicateType2(s1: String, s2: String, s3: String, s4: String, s5: String, s6: String): Boolean =
      s1 == "draught" && s2 == "speed" && s3 == "beaufort" && s4 == "added_resistance" &&
        s5 == "consumption" && s6 == "imo"

    def predicateType3(s1: String, s2: String, s3: String, s4: String, s5: String, s6: String, s7: String): Boolean =
      s1 == "draught" && s2 == "speed_through_water" && s3 == "beaufort" &&
        s4 == "consumption_main_engine_mt_per_day" && s5 == "consumption_auxiliary_engine_mt_per_day" &&
        s6 == "consumption_boiler_engine_mt_per_day" && s7 == "imo"

    def predicateType4(s1: String, s2: String, s3: String, s4: String, s5: String, s6: String): Boolean =
      s1 == "draught" && s2 == "speed" && s3 == "beaufort" &&
        s4 == "trim" && s5 == "consumption" && s6 == "imo"

    xs.map(_.replace('"', ' ').trim) match {
      case c1 :: c2 :: c3 :: c4 :: c5 :: Nil if predicateType1(c1, c2, c3, c4, c5) =>
        {
          case c1 :: c2 :: c3 :: c4 :: c5 :: Nil =>
            for {
              draught <- Try(c1.toDouble).toEither
              speed <- Try(c2.toDouble).toEither
              beaufort <- Try(c3.toDouble).toEither
              consumption <- Try(c4.toDouble).toEither
              imo <- Try(c5.toInt).toEither
            } yield FuelConsumptionDataType1(imo, draught, speed, beaufort, consumption)
          case e => Left(new Exception(s"Unable to parse row to predicateType1: $e"))
        }
      case c1 :: c2 :: c3 :: c4 :: c5 :: c6 :: Nil if predicateType2(c1, c2, c3, c4, c5, c6) => {
        case c1 :: c2 :: c3 :: c4 :: c5 :: c6 :: Nil =>
          for {
            draught <- Try(c1.toDouble).toEither
            speed <- Try(c2.toDouble).toEither
            beaufort <- Try(c3.toDouble).toEither
            addedResistance <- Try(c4.toInt).toEither
            consumption <- Try(c5.toDouble).toEither
            imo <- Try(c6.toInt).toEither
          } yield FuelConsumptionDataType2(imo, draught, speed, beaufort, consumption, addedResistance)
        case e => Left(new Exception(s"Unable to parse row to predicateType2: $e"))
      }
      case c1 :: c2 :: c3 :: c4 :: c5 :: c6 :: c7 :: Nil if predicateType3(c1, c2, c3, c4, c5, c6, c7) => {
        case c1 :: c2 :: c3 :: c4 :: c5 :: c6 :: c7 :: Nil =>
          for {
            draught <- Try(c1.toDouble).toEither
            speed <- Try(c2.toDouble).toEither
            beaufort <- Try(c3.toDouble).toEither
            consumptionMainEngineMtPerDay <- Try(c4.toDouble).toEither
            consumptionAuxiliaryEngineMtPerDay <- Try(c5.toDouble).toEither
            consumptionBoilerEngineMtPerDay <- Try(c6.toDouble).toEither
            imo <- Try(c7.toInt).toEither
          } yield FuelConsumptionDataType3(
             imo,
             draught,
             speed,
             beaufort,
             consumptionMainEngineMtPerDay,
             consumptionAuxiliaryEngineMtPerDay,
             consumptionBoilerEngineMtPerDay
            )
        case e => Left(new Exception(s"Unable to parse row to predicateType3: $e"))
      }
      case c1 :: c2 :: c3 :: c4 :: c5 :: c6 :: Nil if predicateType4(c1, c2, c3, c4, c5, c6) => {
        case c1 :: c2 :: c3 :: c4 :: c5 :: c6 :: Nil =>
          for {
            draught <- Try(c1.toDouble).toEither
            speed <- Try(c2.toDouble).toEither
            beaufort <- Try(c3.toDouble).toEither
            trim <- Try(c4.toDouble).toEither
            consumption <- Try(c5.toDouble).toEither
            imo <- Try(c6.toInt).toEither
          } yield FuelConsumptionDataType4(imo, draught, speed, beaufort, consumption, trim)
        case e => Left(new Exception(s"Unable to parse row to predicateType4: $e"))
      }
      case e => _ => Left(new Exception(s"no predicate type found for parser row: $e"))
    }
  }

  def parseFile(file: JPath)(parallelism: Int): IO[List[Either[Throwable, FuelConsumptionData]]] = {
    for {
      header <- Files[IO].readAll(Path.fromNioPath(file)).through(parser).head.compile.toList.map(_.flatten)
      result <- Files[IO].readAll(Path.fromNioPath(file)).through(parser)
        .drop(1).parEvalMapUnordered(parallelism) { xs =>
        IO.pure(getParser(header)(xs))
      }.compile.toList
    } yield {
      result
    }
  }
}
