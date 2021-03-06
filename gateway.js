require("./opentelemetry");
const { ApolloServer } = require("apollo-server");
const { ApolloGateway } = require("@apollo/gateway");
const { ApolloServerPluginInlineTrace } = require("apollo-server-core");

const gateway = new ApolloGateway({
    // This entire `serviceList` is optional when running in managed federation
    // mode, using Apollo Graph Manager as the source of truth.  In production,
    // using a single source of truth to compose a schema is recommended and
    // prevents composition failures at runtime using schema validation using
    // real usage-based metrics.
    serviceList: [
        { name: "accounts", url: "http://127.0.0.1:4001/graphql" },
        { name: "reviews", url: "http://127.0.0.1:4002/graphql" },
        { name: "products", url: "http://127.0.0.1:4003/graphql" },
        { name: "inventory", url: "http://127.0.0.1:4004/graphql" }
    ],

    // Experimental: Enabling this enables the query plan view in Playground.
    __exposeQueryPlanExperimental: false,
});

(async () => {
    const server = new ApolloServer({
        gateway,

        // Apollo Graph Manager (previously known as Apollo Engine)
        // When enabled and an `ENGINE_API_KEY` is set in the environment,
        // provides metrics, schema management and trace reporting.
        engine: false,

        subscriptions: false,

        plugins: [ApolloServerPluginInlineTrace()],
    });

    server.listen().then(({ url }) => {
        console.log(`🚀 Server ready at ${url}`);
    });
})();
