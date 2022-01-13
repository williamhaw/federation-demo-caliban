# federation-demo-caliban

This repository reimplements Apollo's [federation-demo](https://github.com/apollographql/federation-demo) using Scala
and Caliban.

## Contents
| Service   | Api                                                                                              | Server                                                                                                 |
|-----------|--------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| Accounts  | [AccountsApi](accounts/src/main/scala/com/williamhaw/gql_caliban/accounts/AccountsApi.scala)     | [AccountsServer](accounts/src/main/scala/com/williamhaw/gql_caliban/accounts/AccountsServer.scala)     |
| Reviews   | [ReviewsApi]( reviews/src/main/scala/com/williamhaw/gql_caliban/reviews/ReviewsApi.scala)        | [ReviewsServer](reviews/src/main/scala/com/williamhaw/gql_caliban/reviews/ReviewsServer.scala)         |
| Products  | [ProductsApi](products/src/main/scala/com/williamhaw/gql_caliban/products/ProductsApi.scala)     | [ProductsServer](products/src/main/scala/com/williamhaw/gql_caliban/products/ProductsServer.scala)     |
| Inventory | [InventoryApi](inventory/src/main/scala/com/williamhaw/gql_caliban/inventory/InventoryApi.scala) | [InventoryServer](inventory/src/main/scala/com/williamhaw/gql_caliban/inventory/InventoryServer.scala) |

## Requirements

- sbt > 1.x
- recent version of npm and node

## Setup and Run

1. Run all servers by starting the sbt shell:
   ```bash
    > sbt
   ```
2. Inside the sbt shell, run:
   ```
   ...
   [info] started sbt server
   sbt:federation-demo-caliban> run AllServers
   ```
3. In the repository root, run
   ```bash
   > npm install
   ```
4. Then run
   ```bash
   > node gateway.js
   ```
6. Navigate to http://localhost:4000 in your preferred browser.
7. Close both gateway and services by hitting `Ctrl-C`.

## Example Queries

```
query ExampleQuery {
  me {
    id
    name
    username
    reviews {
      body
    }
  }
  topProducts(first: 3) {
    upc
    name
    price
    weight
    inStock
    shippingEstimate
    reviews {
      body
    }
  }
}
```

## Notes

- AccountsServer uses Play to serve GraphQL Playground and run the GQL interpreter. The other servers use Akka HTTP
  directly.