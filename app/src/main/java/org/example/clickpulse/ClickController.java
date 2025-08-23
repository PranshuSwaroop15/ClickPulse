package org.example.clickpulse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/click")
public class ClickController {
    private final KafkaProducerService producer;

    public ClickController(KafkaProducerService producer) {
        this.producer = producer;
    }

    @GetMapping("/ping")
    public String ping() { return "pong"; }

    @PostMapping
    public ResponseEntity<String> sendClick(@RequestBody(required = false) String body) {
        producer.sendClick(body == null ? "{}" : body);
        return ResponseEntity.ok("queued");
    }
}
