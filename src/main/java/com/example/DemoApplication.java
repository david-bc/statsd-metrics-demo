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
public class DemoApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	private final MetricRegistry reg;
	private final Counter counter;
    private final CounterService counterService;

	public DemoApplication(MetricRegistry reg, CounterService counterService) {
		this.reg = reg;
		this.counter = reg.counter("counter.ae.rest.testing");
        this.counterService = counterService;
    }

	@GetMapping
	public boolean in() {
		counter.inc();
		reg.counter("counter.ae.on.the.fly").inc();
        counterService.increment("counter.ae.cs.testing");
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

    @Override
    public void run(String... args) throws Exception {
        ConsoleReporter writer = ConsoleReporter.forRegistry(reg)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();

        writer.start(10, TimeUnit.SECONDS);
    }
}
