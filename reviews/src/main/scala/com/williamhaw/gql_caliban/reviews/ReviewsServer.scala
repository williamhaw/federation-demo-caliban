package com.williamhaw.gql_caliban.reviews

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import caliban.GraphQL.graphQL
import caliban.federation._
import caliban.schema.ArgBuilder
import caliban.{AkkaHttpAdapter, RootResolver}
import sttp.tapir.json.play._
import zio.query.ZQuery
import zio.{Runtime, UIO}

import java.util.UUID
import scala.concurrent.ExecutionContextExecutor

object ReviewsServer extends App {

  @GQLKey("id")
  case class Review(
      id: UUID,
      body: Option[String],
      @GQLProvides("username")
      author: Option[User],
      product: Option[Product]
  )

  @GQLKey("id")
  @GQLExtend
  case class User(@GQLExternal id: UUID, @GQLExternal username: Option[String], reviews: Seq[Review])

  @GQLKey("upc")
  @GQLExtend
  case class Product(@GQLExternal upc: String, reviews: Seq[Review])

  case class ReviewInternal(
      id: UUID,
      body: Option[String],
      author: Option[UserInternal],
      product: Option[ProductInternal]
  )
  case class UserInternal(id: UUID)
  case class ProductInternal(upc: String)

  val users = Seq(
    UserInternal(UUID.fromString("da02636a-a240-4607-bf63-85b4f76ec6f1")),
    UserInternal(UUID.fromString("ad8f41df-c5d2-4a46-91a7-f637f9a08060"))
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

  case class ReviewArgs(id: UUID)
  case class ReviewResolvedArgs(id: UUID, author: User, product: Product)
  case class UserArgs(id: UUID)
  case class ProductArgs(upc: String)

  implicit lazy val userArgBuilder: ArgBuilder[User]       = ArgBuilder.gen[User]
  implicit lazy val productArgBuilder: ArgBuilder[Product] = ArgBuilder.gen[Product]

  case class Queries(review: ReviewArgs => Option[Review])

  val api = graphQL(RootResolver()) @@ federated(
    EntityResolver.from[ReviewResolvedArgs](args =>
      ZQuery.fromEffect(UIO(reviews.find(_.id == args.id).map { r =>
        Review(r.id, r.body, Some(args.author), Some(args.product))
      }))
    ),
    EntityResolver.from[UserArgs](args =>
      ZQuery.fromEffect(
        UIO.succeed(
          users.find(_.id == args.id).map { u =>
            User(
              u.id,
              None,
              reviews.filter(_.author.exists(_.id.equals(u.id))).map(r => Review(r.id, r.body, None, None))
            )
          }
        )
      )
    ),
    EntityResolver.from[ProductArgs](args =>
      ZQuery.fromEffect(
        UIO.succeed(
          if (reviews.exists(_.product.exists(_.upc == args.upc)))
            Some(
              Product(
                args.upc,
                reviews.filter(_.product.exists(_.upc == args.upc)).map(r => Review(r.id, r.body, None, None))
              )
            )
          else
            None
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
      // Point browser to http://localhost:4002/graphql for GraphQL Playground
      getFromResource("graphql-playground.html")
    } ~ path("graphql") {
      AkkaHttpAdapter.makeHttpService(interpreter)
    }

  Http().newServerAt("localhost", 4002).bind(route)

}
