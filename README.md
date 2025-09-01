# ClickPulse : A Real-time Monitoring Platform for Click Events

ClickPulse is a full-stack real-time monitoring platform that tracks and visualizes user click behaviors - **Add to Cart vs Buy Now actions**. It provides a backend powered by Spring Boot and Kafka, real-time analytics with Prometheus & Grafana, and a frontend built with React.
##
# 1. Architecture Overview

* Backend + Message Broker: Spring Boot service with Kafka integration and  Kafka handles event streaming.

* Monitoring and Visualizaton : Prometheus scrapes metrics from the backend every 5s and Grafana dashboards provide real-time insights.

* Frontend: React (Vite) product page for user interactions and live stats.

* Deployment: Hosted on AWS Lightsail (Ubuntu).

  **[Architecture Diagram](https://miro.com/app/board/uXjVJN3tNLU=/?share_link_id=841833978868)**


## 

# 2. Backend with Spring Boot + Kafka

Started a new Spring Boot (Gradle) project.

Added dependencies: spring-boot-starter-web, spring-kafka, spring-boot-starter-actuator, micrometer-registry-prometheus.

Built REST endpoints:

POST /api/action → publish a click event to Kafka (add_to_cart / buy_now).

GET /api/stats → return aggregated counters for the frontend.

Added a Kafka consumer with @KafkaListener that consumes events and increments Micrometer counters:

`clickpulse_actions_processed_total{action="add_to_cart"} 123`
<br/>
`clickpulse_actions_processed_total{action="buy_now"} 45`

Verified locally with Kafka CLI producer/consumer.
<img width="1920" height="1010" alt="Screenshot (297)" src="https://github.com/user-attachments/assets/573d9c0b-cf97-4f9f-940e-f9d517588de6" />

##
# 3. Metrics Monitoring with Prometheus and Grafana

Enabled /actuator/prometheus in Spring Boot.

<img width="1920" height="875" alt="Screenshot (298)" src="https://github.com/user-attachments/assets/898674b1-61a9-4a46-8bf6-d2f4658d68fc" />


Configured Prometheus on the VM to scrape the backend every 5s.

<img width="1918" height="860" alt="Prometheus table" src="https://github.com/user-attachments/assets/675497b9-b20b-42f5-b489-d76facd5b56f" />




Imported a Grafana dashboard:

`Query: sum by(action) (clickpulse_actions_processed_total)`
<br/>
`Query: sum by(action) (rate(clickpulse_actions_processed_total[5m]))`

Verified real-time graphs of “Add to Cart” vs “Buy Now.”

<img width="1920" height="960" alt="Screenshot (293)" src="https://github.com/user-attachments/assets/f1ab777f-c729-4d10-85c5-6f19da74ea7c" />

<img width="1920" height="970" alt="Screenshot (299)" src="https://github.com/user-attachments/assets/3618ed4c-ca28-4b0f-a034-5c12a30dea95" />

##

# 4. Frontend with React (Vite) [Live Link](http://44.201.241.55/)

Bootstrapped a Vite project.

Built a product page:

Image + price + description

Buttons: Add to Cart |  Buy Now

Each button calls the backend:

POST /api/action with {action:"add_to_cart"}

Polls GET /api/stats every 3s to update totals

UI shows:

Totals per button

Rate (clicks/sec)

Leader (who’s ahead)

<img width="1702" height="1405" alt="screencapture-44-201-241-55-2025-09-01-18_22_00" src="https://github.com/user-attachments/assets/715eedee-b979-46d3-b8bd-8678da3ff8bb" />

## 
# 5. Deployment on AWS Lightsail (Ubuntu)

* Provisioned a VM (Ubuntu).

* Installed Kafka + Zookeeper for local cluster.

* Installed Prometheus and Grafana.

* Built backend JAR and deployed as a systemd service:

* Configured Nginx:

  * Serves frontend static files (/)

  * Proxies /api/* → backend (8080)


## 6. Results

* Visit the public IP → see a product page.

* Click “Add to Cart” or “Buy Now.”

* Backend events go into Kafka, processed by Spring consumer.

* Metrics update in Prometheus → Grafana dashboard visualizes real-time stats.

* Frontend polls /api/stats → shows live totals and leader.
