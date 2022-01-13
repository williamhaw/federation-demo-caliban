package com.williamhaw.gql_caliban.products

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import caliban.AkkaHttpAdapter
import sttp.tapir.json.play._
import zio.Runtime

import scala.concurrent.ExecutionContextExecutor

object ProductsServer extends App {

  println(ProductsApi.api.render)

  implicit val system: ActorSystem                        = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val runtime: Runtime[zio.ZEnv]                 = Runtime.global

  val interpreter = runtime.unsafeRun(ProductsApi.api.interpreter)

  val route =
    path("graphql") {
      // Point browser to http://localhost:4003/graphql for GraphQL Playground
      getFromResource("graphql-playground.html")
    } ~ path("graphql") {
      /*
       curl -X POST \
       http://localhost:4003/graphql \
       -H 'Host: localhost:4003' \
       -H 'Content-Type: application/json' \
       -d '{
       "query": "query { topProducts(first: 5) { upc, name, price, weight }}"
       }'
       */
      AkkaHttpAdapter.makeHttpService(interpreter)
    }

  Http().newServerAt("localhost", 4003).bind(route)

}
