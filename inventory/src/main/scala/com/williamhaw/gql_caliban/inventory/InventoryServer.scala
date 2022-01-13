package com.williamhaw.gql_caliban.inventory

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import caliban.AkkaHttpAdapter
import sttp.tapir.json.play._
import zio.Runtime

import scala.concurrent.ExecutionContextExecutor

object InventoryServer extends App {

  println(InventoryApi.api.render)

  implicit val system: ActorSystem                        = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val runtime: Runtime[zio.ZEnv]                 = Runtime.global

  val interpreter = runtime.unsafeRun(InventoryApi.api.interpreter)

  val route =
    path("graphql") {
      // Point browser to http://localhost:4004/graphql for GraphQL Playground
      getFromResource("graphql-playground.html")
    } ~ path("graphql") {
      AkkaHttpAdapter.makeHttpService(interpreter)
    }

  Http().newServerAt("localhost", 4004).bind(route)

}
