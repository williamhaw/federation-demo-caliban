package com.williamhaw.gql_caliban.products

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import caliban.GraphQL.graphQL
import caliban.federation._
import caliban.{AkkaHttpAdapter, RootResolver}
import sttp.tapir.json.play._
import zio.Runtime

import scala.concurrent.ExecutionContextExecutor

object ProductsServer extends App {

  @GQLKey("upc")
  case class Product(upc: String, name: String, price: Int, weight: Int)

  val products = Seq(
    Product("1", "Table", 899, 100),
    Product("2", "Couch", 1299, 1000),
    Product("3", "Chair", 54, 50)
  )

  def topProducts(args: TopArgs): Seq[Product] = products.slice(0, args.first)

  case class TopArgs(first: Int)

  case class Queries(topProducts: TopArgs => Seq[Product])

  val queries: Queries = Queries(args => topProducts(args))

  val api = graphQL(RootResolver(queries))

  implicit val system: ActorSystem                        = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val runtime: Runtime[zio.ZEnv]                 = Runtime.global

  val interpreter = runtime.unsafeRun(api.interpreter)

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
