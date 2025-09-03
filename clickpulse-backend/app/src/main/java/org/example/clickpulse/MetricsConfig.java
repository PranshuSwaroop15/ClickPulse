package org.example.clickpulse;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    public static final String METRIC = "clickpulse_actions_processed_total";

    @Bean
    MeterBinder clickpulseBinder() {
        // Pre-register counters at 0 so they show up before any clicks
        return (MeterRegistry registry) -> {
            registry.counter(METRIC, "action", "add_to_cart");
            registry.counter(METRIC, "action", "buy_now");
        };
    }
}
