package com.williamhaw.gql_caliban.products

import caliban.GraphQL.graphQL
import caliban.RootResolver
import caliban.federation._
import zio.UIO
import zio.query.ZQuery

object ProductsApi {

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

}
