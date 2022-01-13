import Dependencies._

ThisBuild / scalaVersion     := "2.13.7"
ThisBuild / organization     := "com.williamhaw"
ThisBuild / organizationName := "gql-caliban"

lazy val root = (project in file("."))
  .settings(
    name := "federation-demo-caliban",
    libraryDependencies ++= commonDependencies
  )
  .dependsOn(accounts, products, inventory, reviews, resources)
  .aggregate(accounts, products, inventory, reviews)

lazy val accounts = (project in file("accounts"))
  .settings(libraryDependencies ++= commonDependencies)
  .dependsOn(resources)

lazy val products = (project in file("products"))
  .settings(libraryDependencies ++= commonDependencies)
  .dependsOn(resources)

lazy val inventory = (project in file("inventory"))
  .settings(libraryDependencies ++= commonDependencies)
  .dependsOn(resources)

lazy val reviews = (project in file("reviews"))
  .settings(libraryDependencies ++= commonDependencies)
  .dependsOn(resources)

lazy val resources = (project in file("resources"))

lazy val commonDependencies = Seq(
  caliban,
  calibanFederation,
  calibanAkkaHttp,
  calibanPlay,
  slf4j,
  scalaTest % Test
)
