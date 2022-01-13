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

  case class ProductInternal(upc: String, inStock: Boolean)

  val inventory = Seq(
    ProductInternal("1", inStock = true),
    ProductInternal("2", inStock = false),
    ProductInternal("3", inStock = true)
  )

  case class ProductArgs(upc: String, price: Option[Int], weight: Option[Int])

  val api = graphQL(RootResolver()) @@ federated(
    EntityResolver.from[ProductArgs](args =>
      ZQuery.fromEffect(
        UIO(
          inventory
            .find(_.upc == args.upc)
            .map { p =>
              // free for expensive items
              if (args.price.forall(_ > 1000)) {
                Product(p.upc, args.weight, args.price, inStock = Some(p.inStock), shippingEstimate = Some(0))
              } else
                // estimate is based on weight
                Product(
                  p.upc,
                  args.weight,
                  args.price,
                  inStock = Some(p.inStock),
                  shippingEstimate = Some((args.weight.getOrElse(0) * 0.5).toInt)
                )
            }
        )
      )
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
