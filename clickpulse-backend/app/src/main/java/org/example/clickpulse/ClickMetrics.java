package org.example.clickpulse;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class ClickMetrics {
    private final MeterRegistry registry;
    private final ConcurrentMap<String, Counter> byTag = new ConcurrentHashMap<>();

    public ClickMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void inc(String tag, double n) {
        // registry.counter(...) returns a cached Counter per unique tag
        Counter c = byTag.computeIfAbsent(tag, t ->
                registry.counter("clickpulse_clicks_consumed_total", "tag", t)
        );
        c.increment(n);
    }

    // Optional: total (no tag)
    public void incTotal(double n) {
        registry.counter("clickpulse_clicks_consumed_total").increment(n);
    }
}
