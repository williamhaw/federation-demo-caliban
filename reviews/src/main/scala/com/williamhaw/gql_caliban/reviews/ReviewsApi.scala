package com.williamhaw.gql_caliban.reviews

import caliban.GraphQL.graphQL
import caliban.RootResolver
import caliban.federation._
import zio.query.ZQuery

import java.util.UUID

object ReviewsApi {

  @GQLKey("id")
  case class Review(
      id: UUID,
      body: Option[String],
      @GQLProvides("username")
      author: ZQuery[Any, Nothing, Option[User]],
      product: ZQuery[Any, Nothing, Option[Product]]
  )

  @GQLKey("id")
  @GQLExtend
  case class User(
      @GQLExternal id: UUID,
      @GQLExternal username: Option[String],
      reviews: ZQuery[Any, Nothing, Seq[Review]]
  )

  @GQLKey("upc")
  @GQLExtend
  case class Product(@GQLExternal upc: String, reviews: ZQuery[Any, Nothing, Seq[Review]])

  case class ReviewInternal(
      id: UUID,
      body: Option[String],
      author: Option[UserInternal],
      product: Option[ProductInternal]
  )
  case class UserInternal(id: UUID, username: Option[String])
  case class ProductInternal(upc: String)

  val users = Seq(
    UserInternal(UUID.fromString("da02636a-a240-4607-bf63-85b4f76ec6f1"), Some("@ada")),
    UserInternal(UUID.fromString("ad8f41df-c5d2-4a46-91a7-f637f9a08060"), Some("@complete"))
  )

  val reviews = Seq(
    ReviewInternal(
      UUID.fromString("a6c43de7-5eab-4c83-97bf-66b44cf533e8"),
      Some("Love it!"),
      Some(users.head),
      Some(ProductInternal("1"))
    ),
    ReviewInternal(
      UUID.fromString("e4e8f4af-0161-4dad-8d2c-6c713381503f"),
      Some("Too expensive."),
      Some(users.head),
      Some(ProductInternal("2"))
    ),
    ReviewInternal(
      UUID.fromString("add48364-a071-49a9-9b67-db95a22ba99a"),
      Some("Could be better."),
      Some(users(1)),
      Some(ProductInternal("3"))
    ),
    ReviewInternal(
      UUID.fromString("aa5381ef-1531-4d6a-af09-6c8bd0c49843"),
      Some("Prefer something else."),
      Some(users(1)),
      Some(ProductInternal("1"))
    )
  )

  def getReviewsForUser(userId: UUID): Seq[Review] = reviews.filter(_.author.exists(_.id == userId)).map { r =>
    Review(r.id, r.body, userQuery(r.author), productQuery(r.product))
  }

  def getReviewsForProduct(upc: String): Seq[Review] = reviews.filter(_.product.exists(_.upc == upc)).map { r =>
    Review(r.id, r.body, userQuery(r.author), productQuery(r.product))
  }

  val userQuery: Option[UserInternal] => ZQuery[Any, Nothing, Option[User]] = (ui: Option[UserInternal]) =>
    ZQuery.foreach(ui)(u => ZQuery.succeed(User(u.id, u.username, ZQuery.succeed(getReviewsForUser(u.id)))))
  val productQuery: Option[ProductInternal] => ZQuery[Any, Nothing, Option[Product]] = (pi: Option[ProductInternal]) =>
    ZQuery.foreach(pi)(p => ZQuery.succeed(Product(p.upc, ZQuery.succeed(getReviewsForProduct(p.upc)))))

  case class ReviewArgs(id: UUID)
  case class UserArgs(id: UUID, username: Option[String])
  case class ProductArgs(upc: String)

  val api = graphQL(RootResolver()) @@ federated(
    EntityResolver.from[ReviewArgs](args =>
      ZQuery.succeed(reviews.find(_.id == args.id).map { r =>
        Review(r.id, r.body, userQuery(r.author), productQuery(r.product))
      })
    ),
    EntityResolver.from[UserArgs](args => userQuery(Some(UserInternal(args.id, args.username)))),
    EntityResolver.from[ProductArgs](args => productQuery(Some(ProductInternal(args.upc))))
  )

}
