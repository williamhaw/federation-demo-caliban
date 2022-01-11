package com.williamhaw.gql_caliban.reviews

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import caliban.GraphQL.graphQL
import caliban.federation._
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
  @GQLExternal
  case class User(@GQLExternal id: UUID, @GQLExternal username: Option[String] = None, reviews: Seq[Review] = Seq.empty)

  @GQLKey("upc")
  @GQLExternal
  case class Product(@GQLExtend upc: String, reviews: Seq[Review])

  val users = Seq(
    User(UUID.fromString("da02636a-a240-4607-bf63-85b4f76ec6f1"), Some("@ada"), Seq.empty),
    User(UUID.fromString("ad8f41df-c5d2-4a46-91a7-f637f9a08060"), Some("@complete"), Seq.empty)
  )

  val reviews = Seq(
    Review(
      UUID.fromString("a6c43de7-5eab-4c83-97bf-66b44cf533e8"),
      Some("Love it!"),
      Some(User(UUID.fromString("da02636a-a240-4607-bf63-85b4f76ec6f1"))),
      Some(Product("1", Seq.empty))
    ),
    Review(
      UUID.fromString("e4e8f4af-0161-4dad-8d2c-6c713381503f"),
      Some("Too expensive."),
      Some(User(UUID.fromString("da02636a-a240-4607-bf63-85b4f76ec6f1"))),
      Some(Product("2", Seq.empty))
    ),
    Review(
      UUID.fromString("add48364-a071-49a9-9b67-db95a22ba99a"),
      Some("Could be better."),
      Some(User(UUID.fromString("ad8f41df-c5d2-4a46-91a7-f637f9a08060"))),
      Some(Product("3", Seq.empty))
    ),
    Review(
      UUID.fromString("aa5381ef-1531-4d6a-af09-6c8bd0c49843"),
      Some("Prefer something else."),
      Some(User(UUID.fromString("ad8f41df-c5d2-4a46-91a7-f637f9a08060"))),
      Some(Product("1", Seq.empty))
    )
  )

  case class ReviewArgs(id: UUID)
  case class UserArgs(authorID: UUID)
  case class ProductArgs(upc: String)

  case class Queries()

  val queries: Queries = Queries()

  val api = graphQL(RootResolver(queries)) @@ federated(
    EntityResolver.from[ReviewArgs](args => ZQuery.fromEffect(UIO(reviews.find(_.id == args.id))))
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
