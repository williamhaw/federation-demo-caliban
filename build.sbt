import Dependencies._

ThisBuild / scalaVersion     := "2.13.7"
ThisBuild / organization     := "com.williamhaw"
ThisBuild / organizationName := "gql-caliban"

lazy val root = (project in file("."))
  .settings(
    name := "federation-demo-caliban"
  )
  .aggregate(accounts, products, inventory, reviews)

lazy val accounts = (project in file("accounts"))
  .settings(libraryDependencies ++= commonDependencies)

lazy val products = (project in file("products"))
  .settings(libraryDependencies ++= commonDependencies)

lazy val inventory = (project in file("inventory"))
  .settings(libraryDependencies ++= commonDependencies)

lazy val reviews = (project in file("reviews"))
  .settings(libraryDependencies ++= commonDependencies)

lazy val commonDependencies = Seq(
  caliban,
  calibanFederation,
  calibanAkkaHttp,
  calibanPlay,
  scalaTest % Test
)
