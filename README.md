# federation-demo-caliban

This repository reimplements Apollo's [federation-demo](https://github.com/apollographql/federation-demo) using Scala
and Caliban.

# Requirements

- sbt > 1.x
- recent version of npm and node

# Setup and Run

1. Run [AccountsServer](accounts/src/main/scala/com/williamhaw/gql_caliban/accounts/AccountsServer.scala),
   [InventoryServer](inventory/src/main/scala/com/williamhaw/gql_caliban/inventory/InventoryServer.scala),
   [ProductsServer](products/src/main/scala/com/williamhaw/gql_caliban/products/ProductsServer.scala) and
   [ReviewsServer](reviews/src/main/scala/com/williamhaw/gql_caliban/reviews/ReviewsServer.scala)

   Currently I run them using Intellij.
2. `npm install`
3. `node gateway.js`
4. Navigate to http://localhost:4000 in your preferred browser.

# Example Queries

```
query ExampleQuery {
  me {
    id
    name
    username
  }
  topProducts(first: 3) {
    upc
    name
    price
    weight
    inStock
    shippingEstimate
  }
}
```