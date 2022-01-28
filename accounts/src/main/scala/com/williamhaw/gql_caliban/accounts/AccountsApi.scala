package com.williamhaw.gql_caliban.accounts

import caliban.GraphQL.graphQL
import caliban.RootResolver
import caliban.federation._
import caliban.federation.tracing.ApolloFederatedTracing
import zio.UIO
import zio.query.ZQuery

import java.util.UUID

object AccountsApi {

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
  ) @@ ApolloFederatedTracing.wrapper

}
