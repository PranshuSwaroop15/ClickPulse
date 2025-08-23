//package org.example.clickpulse;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api")
//public class ActionController {
//    private final KafkaTemplate<String, String> kafka;
//    private final String topic;
//
//    public ActionController(KafkaTemplate<String, String> kafka,
//                            @Value("${clickpulse.topic:clickpulse}") String topic) {
//        this.kafka = kafka;
//        this.topic = topic;
//    }
//
//    @PostMapping("/action")
//    public String send(@RequestBody Map<String, Object> body) {
//        String action = String.valueOf(body.getOrDefault("action", "add_to_cart"));
//        kafka.send(topic, action);
//        return "queued: " + action;
//    }
//}

package org.example.clickpulse;

import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ActionController {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MeterRegistry registry;

    public ActionController(KafkaTemplate<String, String> kafkaTemplate,
                            MeterRegistry registry) {
        this.kafkaTemplate = kafkaTemplate;
        this.registry = registry;
    }

    // POST /api/action  -> queues to Kafka immediately and returns 202
    @PostMapping("/action")
    public ResponseEntity<Map<String, Object>> postAction(@RequestBody Map<String, Object> body) {
        String action = String.valueOf(body.getOrDefault("action", "add_to_cart")).trim();
        if (!(action.equals("add_to_cart") || action.equals("buy_now"))) {
            action = "add_to_cart";
        }
        int n = 1;
        try { n = Integer.parseInt(String.valueOf(body.getOrDefault("n", "1"))); }
        catch (Exception ignored) {}

        // enqueue quickly; do not block the HTTP response
        for (int i = 0; i < Math.max(1, n); i++) {
            kafkaTemplate.send("clickpulse", action);
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("queued", action);
        resp.put("n", n);
        return ResponseEntity.accepted().body(resp);
    }

    // GET /api/stats  -> reads Micrometer counters and returns simple JSON for the UI
    @GetMapping("/stats")
    public Map<String, Object> stats() {
        double atc = registry.counter(MetricsConfig.METRIC, "action", "add_to_cart").count();
        double buy = registry.counter(MetricsConfig.METRIC, "action", "buy_now").count();

        List<Map<String, Object>> total = List.of(
                Map.of("action", "add_to_cart", "value", atc),
                Map.of("action", "buy_now", "value", buy)
        );
        // we return 0 rates here; Grafana should use Prometheus for rates
        List<Map<String, Object>> rate = List.of(
                Map.of("action", "add_to_cart", "value", 0),
                Map.of("action", "buy_now", "value", 0)
        );
        return Map.of("total", total, "rate", rate, "ts", System.currentTimeMillis());
    }
}
