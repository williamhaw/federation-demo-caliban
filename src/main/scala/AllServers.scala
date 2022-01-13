import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import caliban.AkkaHttpAdapter
import com.williamhaw.gql_caliban.accounts.AccountsApi
import com.williamhaw.gql_caliban.inventory.InventoryApi
import com.williamhaw.gql_caliban.products.ProductsApi
import com.williamhaw.gql_caliban.reviews.ReviewsApi
import sttp.tapir.json.play._
import zio.Runtime

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object AllServers extends App {

  implicit val system: ActorSystem                        = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val runtime: Runtime[zio.ZEnv]                 = Runtime.global

  val accountsInterpreter = runtime.unsafeRun(AccountsApi.api.interpreter)

  val accountsRoute =
    path("graphql") {
      getFromResource("graphql-playground.html")
    } ~ path("graphql") {
      AkkaHttpAdapter.makeHttpService(accountsInterpreter)
    }

  Http()
    .newServerAt("localhost", 4001)
    .bind(accountsRoute)
    .onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("🚀 Accounts Server ready at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }

  val reviewsInterpreter = runtime.unsafeRun(ReviewsApi.api.interpreter)

  val reviewsRoute =
    path("graphql") {
      getFromResource("graphql-playground.html")
    } ~ path("graphql") {
      AkkaHttpAdapter.makeHttpService(reviewsInterpreter)
    }

  Http()
    .newServerAt("localhost", 4002)
    .bind(reviewsRoute)
    .onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("🚀 Reviews Server ready at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }

  val productsInterpreter = runtime.unsafeRun(ProductsApi.api.interpreter)

  val productsRoute =
    path("graphql") {
      getFromResource("graphql-playground.html")
    } ~ path("graphql") {
      AkkaHttpAdapter.makeHttpService(productsInterpreter)
    }

  Http()
    .newServerAt("localhost", 4003)
    .bind(productsRoute)
    .onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("🚀 Products Server ready at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }

  val inventoryInterpreter = runtime.unsafeRun(InventoryApi.api.interpreter)

  val inventoryRoute =
    path("graphql") {
      getFromResource("graphql-playground.html")
    } ~ path("graphql") {
      AkkaHttpAdapter.makeHttpService(inventoryInterpreter)
    }

  Http()
    .newServerAt("localhost", 4004)
    .bind(inventoryRoute)
    .onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("🚀 Inventory Server ready at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
}
