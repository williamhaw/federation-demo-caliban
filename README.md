# federation-demo-caliban

This repository reimplements Apollo's [federation-demo](https://github.com/apollographql/federation-demo) using Scala
and Caliban.



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
3. `npm install`
4. `node gateway.js`
5. Navigate to http://localhost:4000 in your preferred browser.
6. Close both gateway and services by hitting `Ctrl-C`.

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