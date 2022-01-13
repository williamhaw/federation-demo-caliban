package com.williamhaw.gql_caliban.reviews

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import caliban.AkkaHttpAdapter
import sttp.tapir.json.play._
import zio.Runtime

import scala.concurrent.ExecutionContextExecutor

object ReviewsServer extends App {

  println(ReviewsApi.api.render)

  implicit val system: ActorSystem                        = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val runtime: Runtime[zio.ZEnv]                 = Runtime.global

  val interpreter = runtime.unsafeRun(ReviewsApi.api.interpreter)

  val route =
    path("graphql") {
      // Point browser to http://localhost:4002/graphql for GraphQL Playground
      getFromResource("graphql-playground.html")
    } ~ path("graphql") {
      AkkaHttpAdapter.makeHttpService(interpreter)
    }

  Http().newServerAt("localhost", 4002).bind(route)

}
