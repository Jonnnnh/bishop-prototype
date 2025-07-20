# Bishop Prototype

An application emulator for testing the [Synthetic-Human-Core Starter](https://github.com/Jonnnnh/synthetic-human-core-starter).

---

## Prerequisites

1. Make sure you have installed:

    * Java 17+
    * Gradle 7+
    * (for Docker mode) Docker & Docker Compose
   
2. Build the JAR locally:

   ```bash
   ./gradlew clean bootJar
   ```

   After this, you’ll find `bishop-prototype-*.jar` in `build/libs/`

---

## Local Launch & Testing via Console

1. Start the service:

   ```bash
   ./gradlew bootRun
   ```

> The application listens on port **8080**

2. Send commands:

    * **COMMON** (queued for background processing):

      ```bash
      curl -i -X POST http://localhost:8080/bishop/cmd \
        -H "Content-Type: application/json" \
        -d '{
          "description": "regular task",
          "priority": "COMMON",
          "author": "alice",
          "time": "2025-07-17T12:00:00Z"
        }'
      ```

    * **CRITICAL** (executed immediately @Async):

      ```bash
      curl -i -X POST http://localhost:8080/bishop/cmd \
        -H "Content-Type: application/json" \
        -d '{
          "description": "urgent task",
          "priority": "CRITICAL",
          "author": "bob",
          "time": "2025-07-17T12:00:00Z"
        }'
      ```

> Both requests will return `HTTP/1.1 202 Accepted`

3. Metrics via Actuator:

    * Dispatch queue size:

      ```bash
      curl http://localhost:8080/actuator/metrics/commands.queue.size
      ```

    * Async pool queue size:

      ```bash
      curl http://localhost:8080/actuator/metrics/shc.executor.queue.size
      ```

    * Executed commands counter by author:

      ```bash
      curl "http://localhost:8080/actuator/metrics/commands.executed?tag=author:alice"
      ```

4. AOP Audit to Console

   By default, `app.audit.mode=console`. In the logs you’ll see:

   ```text
   [AUDIT][ENTER] AuditEvent{...}
   [AUDIT][EXIT]  AuditEvent{...}
   ```

---

## Testing via Docker + Kafka

The project root contains `Dockerfile` and `docker-compose.yml` to spin up:

* Kafka in KRaft mode
* Bishop-Prototype with `app.audit.mode=kafka`

### Dockerfile

> Before building the Docker image, be sure to run:
>
> ```bash
> ./gradlew clean bootJar
> ```
>
> This generates the JAR in `build/libs/`

```dockerfile
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
```

### docker-compose.yml

```yaml
services:
  kafka:
    image: bitnami/kafka:3.5.0
    environment:
      - KAFKA_CFG_PROCESS_ROLES=broker,controller
      - KAFKA_CFG_NODE_ID=1
      - KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=1@kafka:9093
      - KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093
      - KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092
      - ALLOW_PLAINTEXT_LISTENER=yes
    ports:
      - "9092:9092"
      - "9093:9093"

  emulator:
    build:
      context: .
      dockerfile: Dockerfile
    depends_on:
      - kafka
    ports:
      - "8080:8080"
    environment:
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      APP_AUDIT_MODE: kafka
      APP_AUDIT_TOPIC: audit.logs
```

### Launching with Docker

1. Build and start the containers:

   ```bash
   docker-compose up --build
   ```

2. Wait for the log line:

   ```text
   Started BishopPrototypeApplication
   ```

### Verification

1. Send commands (same as in local mode):

   ```bash
   curl -i -X POST http://localhost:8080/bishop/cmd \
     -H "Content-Type: application/json" \
     -d '{
       "description": "docker test",
       "priority": "COMMON",
       "author": "alice",
       "time": "2025-07-17T12:00:00Z"
     }'
   ```

2. Metrics:

   ```bash
   curl http://localhost:8080/actuator/metrics/commands.queue.size
   
   curl http://localhost:8080/actuator/metrics/shc.executor.queue.size
   
   curl "http://localhost:8080/actuator/metrics/commands.executed?tag=author:alice"
   ```
