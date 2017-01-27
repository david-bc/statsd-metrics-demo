# Dropwizard StatsD Metrics

## Overview

| Metric Type | Target Chart Type | Description | Example Usecase |
| - | - | - | - |
| Counter | Pie Chart, Bar Chart | A counter is just a gauge for an AtomicLong instance. You can increment or decrement its value. For example, we may want a more efficient way of measuring the pending job in a queue. | Total execution, Total successes |
| Gauge | Gauge, Line/Area Chart | A gauge is an instantaneous measurement of a value. For example, we may want to measure the number of pending jobs in a queue. | Kafka Consumer Lag |
| Meter | Pie Chart, Bar Chart | A meter measures the rate of events over time (e.g., “requests per second”). In addition to the mean rate, meters also track 1-, 5-, and 15-minute moving averages. | Rest requests per second |
| Histogram | Line Chart | A histogram measures the statistical distribution of values in a stream of data. In addition to minimum, maximum, mean, etc., it also measures median, 75th, 90th, 95th, 98th, 99th, and 99.9th percentiles. | Batch Size Metric |
| Timer | Line Chart | A timer measures both the rate that a particular piece of code is called and the distribution of its duration. | Rest endpoint SLA (duration to return) |

## Installation and Setup

- Requires JDK 1.8
- Requires [StatsD Logging Server](https://www.npmjs.com/package/statsd-logger)
  - Once installed run `statsd-logger` on the command line

```
________________________________________________________________________________
| ~/workspace/bettercloud/util @ bc-desposito-mbp (davidesposito)
| => npm install -g statsd-logger
/Users/davidesposito/.npm-global/bin/statsd-logger -> /Users/davidesposito/.npm-global/lib/node_modules/statsd-logger/bin/statsd-logger
/Users/davidesposito/.npm-global/lib
└─┬ statsd-logger@0.0.0
  └── colors@1.1.2
________________________________________________________________________________
| ~/workspace/bettercloud/util @ bc-desposito-mbp (davidesposito)
| => statsd-logger
Server listening on 0.0.0.0:8125
```

## Example Application

Everything is inside of the `com.example.DemoApplication` class. It defines a single
REST endpoints located at `http://localhost:8080/` that will increment multiple metrics
on each execution.

There are two Metrics reporters configured.

- Console Writer: prints all application metrics to console every 10 seconds
- StatsD Reporter: exports ALL metrics (including JVM) to StatsD (`localhost:8125`) every 10 seconds

## Configuration

See `com.example.DemoApplication` for comments.

### Console Writer

This is useful for testing, but you do not need to include it in your production jobs. You can also load it conditionally using Spring Profiles

```
@Bean
@Profile("local")
public CommandLineRunner initConsoleReporter(MetricRegistry metricRegistry) {
  return (args) -> {
    ConsoleReporter writer = ConsoleReporter.forRegistry(reg)
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .build();

    writer.start(10, TimeUnit.SECONDS);
  };
}
```

### StatsD Writer

```
// MetricsConig.java

@Bean
public MetricsEndpointMetricReader metricsEndpointMetricReader(MetricsEndpoint metricsEndpoint) {
  return new MetricsEndpointMetricReader(metricsEndpoint);
}

@Bean
@ExportMetricWriter
public MetricWriter statsdWriter() {
  String prefix = (int) (Math.random() * Integer.MAX_VALUE) + "";
  return new StatsdMetricWriter(prefix, "localhost", 8125);
}
```

```
// application.properties

spring.metrics.export.delay-millis=10000
```

## References

- [Spring Metrics Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-metrics.html#production-ready-dropwizard-metrics)
- [Dropwizard Documentation](http://metrics.dropwizard.io/3.1.0/getting-started/)
- [AOP Spring Metics](http://metrics.ryantenney.com/)
