package com.williamhaw.gql_caliban.inventory

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import caliban.AkkaHttpAdapter
import sttp.tapir.json.play._
import zio.Runtime

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

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

  Http()
    .newServerAt("localhost", 4004)
    .bind(route)
    .onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("🚀 Server ready at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }

}
