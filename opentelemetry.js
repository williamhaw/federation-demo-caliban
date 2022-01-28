// Import required symbols
const { HttpInstrumentation } = require ('@opentelemetry/instrumentation-http');
const { ExpressInstrumentation } = require ('@opentelemetry/instrumentation-express');
const { registerInstrumentations } = require('@opentelemetry/instrumentation');
const { NodeTracerProvider } = require("@opentelemetry/sdk-trace-node");
const { SimpleSpanProcessor, ConsoleSpanExporter } = require ("@opentelemetry/tracing");
const { Resource } = require('@opentelemetry/resources');
const { ZipkinExporter } = require("@opentelemetry/exporter-zipkin");

// Register server-related instrumentation
registerInstrumentations({
    instrumentations: [
        new HttpInstrumentation(),
        new ExpressInstrumentation(),
    ]
});

// Initialize provider and identify this particular service
// (in this case, we're implementing a federated gateway)
const provider = new NodeTracerProvider({
    resource: Resource.default().merge(new Resource({
        // Replace with any string to identify this service in your system
        "service.name": "gateway",
    })),
});

// Configure a test exporter to print all traces to the console
// const consoleExporter = new ConsoleSpanExporter();
// provider.addSpanProcessor(
//     new SimpleSpanProcessor(consoleExporter)
// );

// Configure an exporter that pushes all traces to Zipkin
// (This assumes Zipkin is running on localhost at the
// default port of 9411)
const zipkinExporter = new ZipkinExporter({
    // url: set_this_if_not_running_zipkin_locally
});
provider.addSpanProcessor(
    new SimpleSpanProcessor(zipkinExporter)
);

// Register the provider to begin tracing
provider.register();