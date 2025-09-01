//package org.example.clickpulse;
//
//import io.micrometer.core.instrument.Counter;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//@Component
//public class KafkaConsumerListener {
//    private final Counter clicksCounter;
//
//    public KafkaConsumerListener(Counter clicksConsumedCounter) {
//        this.clicksCounter = clicksConsumedCounter;
//    }
//
//    @KafkaListener(topics = "${clickpulse.topic}", groupId = "${spring.kafka.consumer.group-id}")
//    public void onMessage(String value) {
//        clicksCounter.increment();
//        System.out.println("Consumed: " + value);
//    }
//}
//
//package org.example.clickpulse;
//
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//@Component
//public class KafkaConsumerListener {
//    private final ClickMetrics metrics;
//
//    public KafkaConsumerListener(ClickMetrics metrics) {
//        this.metrics = metrics;
//    }
//
//    @KafkaListener(topics = "${clickpulse.topic}", groupId = "${spring.kafka.consumer.group-id}")
//    public void onMessage(String value) {
//        // Assume messages are "A" or "B" (representing which button was clicked)
//        String tag = value.trim();
//
//        // Increment labeled counter
//        metrics.inc(tag, 1);
//
//        // Optional: also increment a global unlabeled counter
//        // metrics.incTotal(1);
//
//        System.out.println("Consumed: " + value);
//    }
//}
//
//package org.example.clickpulse;
//
//import io.micrometer.core.instrument.Counter;
//import io.micrometer.core.instrument.MeterRegistry;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;
//
//@Component
//public class KafkaConsumerListener {
//    private final MeterRegistry registry;
//    private final ConcurrentMap<String, Counter> counters = new ConcurrentHashMap<>();
//
//    public KafkaConsumerListener(MeterRegistry registry) {
//        this.registry = registry;
//    }
//
//    @KafkaListener(topics = "${clickpulse.topic}", groupId = "${spring.kafka.consumer.group-id}")
//    public void onMessage(String value) {
//        // Create (if absent) a counter labeled by "tag"
//        Counter counter = counters.computeIfAbsent(value, tag ->
//                Counter.builder("clickpulse_clicks_consumed_total")
//                        .description("Total clicks consumed from Kafka, labeled by button tag")
//                        .tag("tag", tag)
//                        .register(registry)
//        );
//
//        counter.increment();
//        System.out.println("Consumed: " + value);
//    }
//}
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
