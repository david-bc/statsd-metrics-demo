package com.example;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Counted;
import com.codahale.metrics.annotation.Gauge;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.ExportMetricWriter;
import org.springframework.boot.actuate.endpoint.MetricsEndpoint;
import org.springframework.boot.actuate.endpoint.MetricsEndpointMetricReader;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.statsd.StatsdMetricWriter;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class DemoApplication {

    /**
     * Runs the spring boot application
     * @param args
     */
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

    @EnableMetrics
    @RestController
    public static class MyMetricsController {

        // Dropwizard metrics API
        private final MetricRegistry metricRegistry;
        // Dropwizard Counter Metric created at initialization time
        private final Counter counter;
        // SpringBoot Counter Service that falls back to Dropwizard counter when Dropwizard is on the classpath.
        private final CounterService counterService;

        public MyMetricsController(MetricRegistry reg, CounterService counterService) {
            this.metricRegistry = reg;
            this.counter = reg.counter("counter.ae.rest.testing");

            this.counterService = counterService;
        }

        /**
         * Define the endpoints that will increment the metrics
         * @return
         */
        @GetMapping
        public boolean in() {
            // increment the pre-initialized metric
            counter.inc();
            // increment a metric on the fly
            metricRegistry.counter("counter.ae.on.the.fly").inc();
            // increment a SpringBoot counter
            counterService.increment("counter.ae.cs.testing");
            // increment a SpringBoot meter
            counterService.increment("meter.ae.cs.meter.testing");
            return true;
        }

        @Metered(name = "meter.ae.testing") // name => com.example.DemoApplication.MyMetricsController.meter.ae.testing
        @GetMapping("/meter")
        public boolean testMetered() {
            return false;
        }

        @Counted(name = "counter.ae.testing")
        @GetMapping("/counter")
        public boolean testCounter() {
            return false;
        }

        @Gauge(name = "gauge.ae.testing")
        @GetMapping("/gauge")
        public boolean testGauge() {
            return false;
        }

        @Timed(name = "timer.ae.testing")
        @GetMapping("/timer")
        public boolean testTimer() {
            return false;
        }
    }

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

    @Bean
    public CommandLineRunner initConsoleReporter(MetricRegistry metricRegistry) {
        return (args) -> {
            ConsoleReporter writer = ConsoleReporter.forRegistry(metricRegistry)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build();

            writer.start(3, TimeUnit.SECONDS);
        };
    }
}
