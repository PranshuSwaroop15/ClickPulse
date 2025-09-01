# ClickPulse – Backend (Spring Boot, Kafka, Prometheus)

**Stack:** Java 17, Spring Boot 3, Gradle, Kafka, Micrometer, Actuator, Prometheus

## What it does
- POST `/api/action` → queues `add_to_cart` / `buy_now` events to Kafka
- `@KafkaListener` consumes the topic and increments Micrometer counter:
  `clickpulse_actions_processed_total{action="add_to_cart|buy_now"}`
- Actuator exposes `/actuator/prometheus` for Prometheus scrape

## Local run
```bash
./gradlew bootRun
# expects Kafka at localhost:9092 (topic: clickpulse)
