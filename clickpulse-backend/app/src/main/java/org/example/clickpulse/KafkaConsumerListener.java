package org.example.clickpulse;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class KafkaConsumerListener {
    private final MeterRegistry registry;
    private final ConcurrentMap<String, Counter> byAction = new ConcurrentHashMap<>();

    public KafkaConsumerListener(MeterRegistry registry) {
        this.registry = registry;
    }

    @org.springframework.kafka.annotation.KafkaListener(
            topics = "clickpulse",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void onMessage(String value) {
        String action = (value == null ? "" : value.trim());
        if (!action.equals("add_to_cart") && !action.equals("buy_now")) action = "add_to_cart";
        registry.counter(MetricsConfig.METRIC, "action", action).increment();
        System.out.println("Consumed: " + action);
    }
}
