package com.williamhaw.gql_caliban.inventory

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import caliban.GraphQL.graphQL
import caliban.{AkkaHttpAdapter, RootResolver}
import caliban.federation._
import sttp.tapir.json.play._
import zio.query.ZQuery
import zio.{Runtime, UIO}

import scala.concurrent.ExecutionContextExecutor

object InventoryServer extends App {

  @GQLKey("upc")
  @GQLExtend
  case class Product(
      @GQLExternal upc: String,
      @GQLExternal weight: Option[Int],
      @GQLExternal price: Option[Int],
      inStock: Option[Boolean],
      @GQLRequires("price weight") shippingEstimate: Option[Int]
  )

  val inventory = Seq(
    Product("1", None, None, inStock = Some(true), None),
    Product("2", None, None, inStock = Some(false), None),
    Product("3", None, None, inStock = Some(true), None)
  )

  case class ProductArgs(upc: String)

  val api = graphQL(RootResolver()) @@ federated(
    EntityResolver.from[ProductArgs](args =>
      ZQuery.fromEffect(UIO(inventory.find(_.upc == args.upc).map { i =>
        // free for expensive items
        if (i.price.forall(_ > 1000))
          i.copy(shippingEstimate = Some(0))
        else
          // estimate is based on weight
          i.copy(shippingEstimate = Some((i.weight.getOrElse(0) * 0.5).toInt))
      }))
    )
  )

  println(api.render)

  implicit val system: ActorSystem                        = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val runtime: Runtime[zio.ZEnv]                 = Runtime.global

  val interpreter = runtime.unsafeRun(api.interpreter)

  val route =
    path("graphql") {
      // Point browser to http://localhost:4004/graphql for GraphQL Playground
      getFromResource("graphql-playground.html")
    } ~ path("graphql") {
      AkkaHttpAdapter.makeHttpService(interpreter)
    }

  Http().newServerAt("localhost", 4004).bind(route)

}
