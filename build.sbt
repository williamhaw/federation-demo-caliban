import Dependencies._

ThisBuild / scalaVersion := "2.13.7"
ThisBuild / organization := "com.williamhaw"
ThisBuild / organizationName := "gql-caliban"

lazy val root = (project in file("."))
  .settings(
    name := "federation-demo-caliban"
  ).aggregate(accounts, products)

lazy val accounts = (project in file("accounts"))
  .settings(libraryDependencies ++= Seq(
    caliban,
    calibanFederation,
    calibanAkkaHttp,
    calibanPlay,
    scalaTest % Test
  ))

lazy val products = (project in file("products"))
  .settings(libraryDependencies ++= Seq(
    caliban,
    calibanFederation,
    calibanAkkaHttp,
    calibanPlay,
    scalaTest % Test
  ))