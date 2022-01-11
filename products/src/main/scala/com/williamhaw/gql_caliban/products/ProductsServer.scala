package com.williamhaw.gql_caliban.products

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import caliban.GraphQL.graphQL
import caliban.federation._
import caliban.{AkkaHttpAdapter, RootResolver}
import sttp.tapir.json.play._
import zio.{Runtime, UIO}
import zio.query.ZQuery

import scala.concurrent.ExecutionContextExecutor

object ProductsServer extends App {

  @GQLKey("upc")
  case class Product(upc: String, name: Option[String], price: Option[Int], weight: Option[Int])

  val products = Seq(
    Product("1", Some("Table"), Some(899), Some(100)),
    Product("2", Some("Couch"), Some(1299), Some(1000)),
    Product("3", Some("Chair"), Some(54), Some(50))
  )

  def topProducts(args: TopArgs): Seq[Product] = products.slice(0, args.first)

  case class TopArgs(first: Int)
  case class ProductArgs(upc: String)

  case class Queries(topProducts: TopArgs => Seq[Product])

  val queries: Queries = Queries(args => topProducts(args))

  val api = graphQL(RootResolver(queries)) @@ federated(
    EntityResolver.from[ProductArgs](args => ZQuery.fromEffect(UIO(products.find(_.upc == args.upc))))
  )

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
