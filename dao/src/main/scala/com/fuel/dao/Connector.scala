package com.fuel.dao

import cats.effect.IO
import com.typesafe.config.ConfigFactory
import doobie.Transactor

object Connector {

  val config = ConfigFactory.load(this.getClass.getClassLoader)

  val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    config.getString("fuel-consumption-dao.driver"),
    config.getString("fuel-consumption-dao.url"),
    config.getString("fuel-consumption-dao.user"),
    config.getString("fuel-consumption-dao.password")
  )

}
