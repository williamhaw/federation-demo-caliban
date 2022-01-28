package com.williamhaw.gql_caliban.inventory

import caliban.GraphQL.graphQL
import caliban.RootResolver
import caliban.federation._
import caliban.federation.tracing.ApolloFederatedTracing
import zio.UIO
import zio.query.ZQuery

object InventoryApi {

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
  ) @@ ApolloFederatedTracing.wrapper

}
