ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.8"

lazy val dao = (project in file("dao"))
  .settings(
    name := "fuel-consumption-dao",
    libraryDependencies ++= Seq(
      Dependencies.doobieCore,
      Dependencies.doobieH2,
      Dependencies.doobieHikari,
      Dependencies.doobiePostgres,
      Dependencies.typeSafeConfig,
      TestDependencies.scalatic,
      TestDependencies.scalaTest,
      TestDependencies.testContainersScalaTest,
      TestDependencies.testContainersPostgres
    )
  ).dependsOn(domain)

lazy val parser = (project in file("parser"))
  .settings(
    assembly / mainClass := Some("com.fuel.parser.init.Main"),
    name := "fuel-consumption-csv-parser",
    libraryDependencies ++= Seq(
      Dependencies.fs2Core,
      Dependencies.fs2IO,
      Dependencies.sl4fjApi,
      Dependencies.sl4fjSimple,
      TestDependencies.scalatic,
      TestDependencies.scalaTest
    )
  ).dependsOn(domain, dao)

lazy val domain = (project in file("domain"))
  .settings(
    name := "fuel-consumption-domain"
  )

lazy val util = (project in file("util"))
  .settings(
    name := "fuel-consumption-util",
    libraryDependencies ++= Seq(
      Dependencies.catsEffects
    )
  )

lazy val service = (project in file("service"))
  .settings(
    assembly / mainClass := Some("com.fuel.service.init.Main"),
    name := "fuel-consumption-service",
    libraryDependencies ++= Seq(
      Dependencies.http4sDsl,
      Dependencies.http4EmberServer,
      Dependencies.http4EmberClient,
      Dependencies.http4EmberCirce,
      Dependencies.circeGeneric,
      TestDependencies.scalatic,
      TestDependencies.scalaTest
    )
  ).dependsOn(dao, util)