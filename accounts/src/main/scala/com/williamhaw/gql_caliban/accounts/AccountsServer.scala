package com.williamhaw.gql_caliban.accounts

import akka.actor.ActorSystem
import akka.http.scaladsl.Http

import java.util.UUID
import caliban.federation._
import caliban.GraphQL.graphQL
import caliban.{AkkaHttpAdapter, RootResolver}
import akka.http.scaladsl.server.Directives._
import zio.{Runtime, UIO}
import sttp.tapir.json.play._
import zio.query.ZQuery

import scala.concurrent.ExecutionContextExecutor

object AccountsServer extends App {

  @GQLKey("id")
  case class User(id: UUID, name: Option[String], username: Option[String])

  val users = Seq(
    User(UUID.fromString("da02636a-a240-4607-bf63-85b4f76ec6f1"), Some("Ada Lovelace"), Some("@ada")),
    User(UUID.fromString("ad8f41df-c5d2-4a46-91a7-f637f9a08060"), Some("Alan Turing"), Some("@complete"))
  )

  def me: User = users.head

  case class UserArgs(id: UUID)

  case class Queries(me: () => User)

  val queries: Queries = Queries(() => me)

  val api = graphQL(RootResolver(queries)) @@ federated(
    EntityResolver.from[UserArgs](args => ZQuery.fromEffect(UIO(users.find(_.id == args.id))))
  )

  println(api.render)

  implicit val system: ActorSystem                        = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val runtime: Runtime[zio.ZEnv]                 = Runtime.global

  val interpreter = runtime.unsafeRun(api.interpreter)

  val route =
    path("graphql") {
      // Point browser to http://localhost:4001/graphql for GraphQL Playground
      getFromResource("graphql-playground.html")
    } ~ path("graphql") {
      /*
       curl -X POST \
       http://localhost:4001/graphql \
       -H 'Host: localhost:4001' \
       -H 'Content-Type: application/json' \
       -d '{
       "query": "query { me { id, name, username }}"
       }'
       */
      AkkaHttpAdapter.makeHttpService(interpreter)
    }

  Http().newServerAt("localhost", 4001).bind(route)
}
