package com.williamhaw.gql_caliban.accounts

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import caliban.PlayAdapter
import play.api.Mode
import play.api.mvc.akkahttp.AkkaHttpHandler
import play.api.routing._
import play.api.routing.sird._
import play.core.server.{AkkaHttpServer, ServerConfig}
import zio.Runtime

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn.readLine

object AccountsServer extends App {

  println(AccountsApi.api.render)

  implicit val system: ActorSystem                        = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val runtime: Runtime[zio.ZEnv]                 = Runtime.global

  val interpreter = runtime.unsafeRun(AccountsApi.api.interpreter)

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

  println("🚀 Server ready at http://localhost:4001/graphql/\nPress RETURN to stop...")
  readLine()
  server.stop()
}
