package com.williamhaw.gql_caliban.accounts

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import caliban.GraphQL.graphQL
import caliban.federation._
import caliban.{PlayAdapter, RootResolver}
import play.api.Mode
import play.api.mvc.akkahttp.AkkaHttpHandler
import play.api.routing._
import play.api.routing.sird._
import play.core.server.{AkkaHttpServer, ServerConfig}
import zio.query.ZQuery
import zio.{Runtime, UIO}

import java.util.UUID
import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn.readLine

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

  val server = AkkaHttpServer.fromRouterWithComponents(
    ServerConfig(
      mode = Mode.Dev,
      port = Some(4001),
      address = "localhost"
    )
  ) { _ =>
    Router.from {
      /*
       curl -X POST \
       http://localhost:4001/graphql \
       -H 'Host: localhost:4001' \
       -H 'Content-Type: application/json' \
       -d '{
       "query": "query { me { id, name, username }}"
       }'
       */
      case req @ POST(p"/graphql") => PlayAdapter.makeHttpService(interpreter).apply(req)
      // Point browser to http://localhost:4001/graphql for GraphQL Playground
      case _ @GET(p"/graphql") =>
        AkkaHttpHandler {
          getFromResource("graphql-playground.html")
        }
    }.routes
  }

  println("Server online at http://localhost:4001/graphql/\nPress RETURN to stop...")
  readLine()
  server.stop()
}
