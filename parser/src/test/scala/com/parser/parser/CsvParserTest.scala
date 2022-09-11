package com.parser.parser

import org.scalatest.flatspec.AsyncFlatSpec
import cats.effect.unsafe.implicits.global
import com.fuel.domain.{FuelConsumptionDataType1, FuelConsumptionDataType2, FuelConsumptionDataType3, FuelConsumptionDataType4}
import com.fuel.parser.parser.CsvParser
import org.scalatest.matchers.should.Matchers
import java.nio.file.Path

class CsvParserTest extends AsyncFlatSpec with Matchers {

  "parseFile" should "parse FuelConsumptionDataType1 correctly " in {

    val path = getClass.getClassLoader.getResource("samples/model1.csv")
    val result = CsvParser.parseFile(Path.of(path.toURI))(200).unsafeToFuture()

    result.map { xs =>
      val result = xs.collect {
        case Right(v) =>
          v match {
            case x: FuelConsumptionDataType1 => x
          }
      }
      result.head shouldEqual FuelConsumptionDataType1(234567, 5.0, 4.0, 8.0, 6.506176376086349)
    }
  }

  it should "parse FuelConsumptionDataType2 correctly " in {

    val path = getClass.getClassLoader.getResource("samples/model2.csv")
    val result = CsvParser.parseFile(Path.of(path.toURI))(200).unsafeToFuture()

    result.map { xs =>
      val result = xs.collect {
        case Right(v) =>
          v match {
            case x: FuelConsumptionDataType2 => x
          }
      }
      result.head shouldEqual FuelConsumptionDataType2(123456, 8.0, 7.0, 0.0,16.76810627955461, 0)
    }
  }

  it should "parse FuelConsumptionDataType3 correctly " in {

    val path = getClass.getClassLoader.getResource("samples/model3.csv")
    val result = CsvParser.parseFile(Path.of(path.toURI))(200).unsafeToFuture()

    result.map { xs =>
      val result = xs.collect {
        case Right(v) =>
          v match {
            case x: FuelConsumptionDataType3 => x
          }
      }
      result.head shouldEqual FuelConsumptionDataType3(345678, 6.0, 6.1, 4.0, 5.237754069305023, 3.6, 1.0)
    }
  }

  it should "parse FuelConsumptionDataType4 correctly " in {

    val path = getClass.getClassLoader.getResource("samples/model4.csv")
    val result = CsvParser.parseFile(Path.of(path.toURI))(200).unsafeToFuture()

    result.map { xs =>
      val result = xs.collect {
        case Right(v) =>
          v match {
            case x: FuelConsumptionDataType4 => x
          }
      }
      result.head shouldEqual FuelConsumptionDataType4(456789, 7.4, 5.0, 0.0, 1.4, 2.8)
    }
  }

}
