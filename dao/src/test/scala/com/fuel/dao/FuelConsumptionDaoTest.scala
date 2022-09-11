package com.fuel.dao

import cats.effect.IO
import com.dimafeng.testcontainers.PostgreSQLContainer
import doobie.Transactor
import org.scalatest.flatspec.AsyncFlatSpec
import org.testcontainers.utility.DockerImageName
import cats.effect.unsafe.implicits.global
import com.fuel.domain.{FuelConsumptionData, FuelConsumptionDataType1, FuelConsumptionDataType2, FuelConsumptionDataType3, FuelConsumptionDataType4, TableQuery}
import org.scalatest.matchers.should.Matchers
import doobie.implicits.toSqlInterpolator
import doobie.implicits._
import org.scalatest.BeforeAndAfterAll
import scala.concurrent.Future

class FuelConsumptionDaoTest extends AsyncFlatSpec with Matchers with BeforeAndAfterAll {

  private val container: PostgreSQLContainer = new PostgreSQLContainer(
    databaseName = Some("database"),
    pgUsername = Some("user"),
    pgPassword = Some("password"),
    dockerImageNameOverride = Option(DockerImageName.parse("postgres:13"))
  )

  container.container.start()

  val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    container.jdbcUrl,
    container.username,
    container.password
  )

  def createTable: Int =
    sql"""create table fuel_consumption(imo int, draught numeric, speed numeric, beaufort numeric, consumption numeric)""".stripMargin.update.run.transact(xa)
      .unsafeRunSync()

  def truncateTable: Future[Int] =
    sql"""truncate table fuel_consumption""".stripMargin.update.run.transact(xa)
      .unsafeToFuture()

  def fixture = new {
    val fuelConsumptionDao = new FuelConsumptionDaoImpl(xa)
  }

  def toUnisonDataType(xs: List[FuelConsumptionData]): List[FuelConsumptionDataType1] =
    xs.map {
      case x: FuelConsumptionDataType1 => x
      case x: FuelConsumptionDataType2 =>
        FuelConsumptionDataType1(x.imo, x.draught, x.speed, x.beaufort, x.consumption)
      case x: FuelConsumptionDataType3 =>
        val consumption = x.consumptionAuxiliaryEngineMtPerDay + x.consumptionBoilerEngineMtPerDay +
          x.consumptionMainEngineMtPerDay
        FuelConsumptionDataType1(x.imo, x.draught, x.speed, x.beaufort, consumption)
      case x: FuelConsumptionDataType4 =>
        FuelConsumptionDataType1(x.imo, x.draught, x.speed, x.beaufort, x.consumption)
    }


  override def beforeAll(): Unit = {
    createTable
  }

  override def afterAll(): Unit = {
    container.stop()
  }

  "bulkInsertFuelConsumptionData" should "insert data correctly" in {
    val f = fixture

    val data = List(
      FuelConsumptionDataType1(123, 1D, 4D, 5D, 4D),
      FuelConsumptionDataType1(123, 1D, 4D, 5D, 4D),
      FuelConsumptionDataType1(123, 1D, 4D, 5D, 4D),
      FuelConsumptionDataType2(123, 2D, 4D, 5D, 4D, 2),
      FuelConsumptionDataType2(123, 2D, 4D, 5D, 4D, 2),
      FuelConsumptionDataType2(123, 2D, 4D, 5D, 4D, 2),
      FuelConsumptionDataType3(123, 3D, 4D, 5D, 4D, 3D, 3D),
      FuelConsumptionDataType3(123, 3D, 4D, 5D, 4D, 3D, 3D),
      FuelConsumptionDataType3(123, 3D, 4D, 5D, 4D, 3D, 3D),
      FuelConsumptionDataType4(123, 4D, 4D, 5D, 4D, 2D),
      FuelConsumptionDataType4(123, 4D, 4D, 5D, 4D, 2D),
      FuelConsumptionDataType4(123, 4D, 4D, 5D, 4D, 2D),
    )

    for {
      _ <- truncateTable
      insert <- f.fuelConsumptionDao.bulkInsertFuelConsumptionData(data).unsafeToFuture()
      result <- f.fuelConsumptionDao.getFuelConsumptionData(123).unsafeToFuture()
    } yield {
      insert shouldBe 12
      result shouldEqual toUnisonDataType(data)
    }
  }

  "getFuelConsumption" should "get closest data row" in {
    val f = fixture

    val data = List(
      FuelConsumptionDataType1(123, 1D, 2D, 3D, 1D),
      FuelConsumptionDataType1(123, 1D, 4D, 5D, 2D),
      FuelConsumptionDataType1(123, 2D, 3D, 4D, 3D),
      FuelConsumptionDataType1(123, 3D, 4D, 5D, 4D),
      FuelConsumptionDataType1(123, 4D, 4D, 5D, 5D),
      FuelConsumptionDataType1(123, 1D, 1D, 5D, 6D),
      FuelConsumptionDataType1(123, 1D, 2D, 5D, 7D)
    )

    val tableQuery = List(
      List(
        TableQuery(123, 2D, 2D, 2D)
      )
    )

    for {
      _ <- truncateTable
      _ <- f.fuelConsumptionDao.bulkInsertFuelConsumptionData(data).unsafeToFuture()
      result <- f.fuelConsumptionDao.getFuelConsumption(tableQuery).unsafeToFuture()
    } yield {
      result shouldEqual List(List(1D))
    }
  }
}
