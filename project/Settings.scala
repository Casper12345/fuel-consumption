import sbt._

object Dependencies {

  val http4sVersion = "0.23.15"

  val http4sDsl = "org.http4s" %% "http4s-dsl" % http4sVersion
  val http4EmberServer = "org.http4s" %% "http4s-ember-server" % http4sVersion
  val http4EmberClient = "org.http4s" %% "http4s-ember-client" % http4sVersion
  val http4EmberCirce = "org.http4s" %% "http4s-circe" % http4sVersion
  val circeGeneric = "io.circe" %% "circe-generic" % "0.14.2"
  val doobieCore = "org.tpolecat" %% "doobie-core" % "1.0.0-RC1"
  val doobieH2 = "org.tpolecat" %% "doobie-h2"        % "1.0.0-RC1"
  val doobieHikari = "org.tpolecat" %% "doobie-hikari"    % "1.0.0-RC1"
  val doobiePostgres = "org.tpolecat" %% "doobie-postgres"  % "1.0.0-RC1"
  val typeSafeConfig = "com.typesafe" % "config" % "1.4.2"
  val catsEffects = "org.typelevel" %% "cats-effect" % "3.3.11"
  val fs2Core = "co.fs2" %% "fs2-core" % "3.2.12"
  val fs2IO = "co.fs2" %% "fs2-io" % "3.2.12"
  val sl4fjApi = "org.slf4j" % "slf4j-api" % "1.7.36"
  val sl4fjSimple = "org.slf4j" % "slf4j-simple" % "1.7.36"

}

object TestDependencies {
  val scalatic  = "org.scalactic" %% "scalactic" % "3.2.13" % Test
  val scalaTest = "org.scalatest" %% "scalatest" % "3.2.13" % Test
  val testContainersScalaTest = "com.dimafeng" %% "testcontainers-scala-scalatest" % "0.40.10" % Test
  val testContainersPostgres = "com.dimafeng" %% "testcontainers-scala-postgresql" % "0.40.10" % Test
}
