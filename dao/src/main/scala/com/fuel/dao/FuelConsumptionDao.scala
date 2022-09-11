package com.fuel.dao

import cats.effect.IO
import doobie._
import doobie.implicits.toSqlInterpolator
import doobie.implicits._
import cats.implicits._
import com.fuel.domain.{FuelConsumptionData, FuelConsumptionDataType1, FuelConsumptionDataType2, FuelConsumptionDataType3, FuelConsumptionDataType4, TableQuery}

trait FuelConsumptionDao[F[_]] {
  def bulkInsertFuelConsumptionData(xs: List[FuelConsumptionData]): F[Int]
  def getFuelConsumptionData(imo: Int): F[List[FuelConsumptionData]]
  def getFuelConsumption(xs: List[List[TableQuery]]): F[List[List[Double]]]
}

class FuelConsumptionDaoImpl(xa: Transactor[IO]) extends FuelConsumptionDao[IO] {

  override def getFuelConsumptionData(imo: Int): IO[List[FuelConsumptionDataType1]] =
    sql"""select * from fuel_consumption where imo=$imo"""
      .query[FuelConsumptionDataType1]
      .to[List]
      .transact(xa)

  def toUnisonDataType(xs: List[FuelConsumptionData]):List[FuelConsumptionDataType1] =
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

  override def getFuelConsumption(xs: List[List[TableQuery]]): IO[List[List[Double]]] =
    xs.map(_.map(consumption).sequence.map(_.flatten)).sequence.transact(xa)

  def consumption(
   q: TableQuery
  ): ConnectionIO[Option[Double]] = { // euclidean distance
    sql"""select consumption from fuel_consumption where
         | imo = ${q.imo} order by((speed - ${q.speed}) * (speed - ${q.speed}) + (draught - ${q.draught}) *
         | (draught - ${q.draught}) + (beaufort - ${q.beaufort}) * (beaufort - ${q.beaufort})) limit 1
       """.stripMargin
      .query[Double]
      .option
  }

  override def bulkInsertFuelConsumptionData(xs: List[FuelConsumptionData]): IO[Int] = {
    val query = """insert into fuel_consumption(imo, draught, speed,beaufort, consumption)
          | values (?,?,?,?,?)""".stripMargin
    Update[FuelConsumptionDataType1](query).updateMany(toUnisonDataType(xs)).transact(xa)
  }
}
