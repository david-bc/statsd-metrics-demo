package com.example;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
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

@RestController
@SpringBootApplication
public class DemoApplication {

    /**
     * Runs the spring boot application
     * @param args
     */
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

    // Dropwizard metrics API
    private final MetricRegistry metricRegistry;
    // Dropwizard Counter Metric created at initialization time
	private final Counter counter;
    // SpringBoot Counter Service that falls back to Dropwizard counter when Dropwizard is on the classpath.
    private final CounterService counterService;

	public DemoApplication(MetricRegistry reg, CounterService counterService) {
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
            ConsoleReporter writer = ConsoleReporter.forRegistry(this.metricRegistry)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build();

            writer.start(10, TimeUnit.SECONDS);
        };
    }
}
