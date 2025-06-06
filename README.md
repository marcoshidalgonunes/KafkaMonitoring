# KafkaMonitoring

KafkaMonitoring is a Spring Boot application designed to monitor the health of Apache Kafka clusters. It periodically checks the status of configured Kafka brokers and logs their health, making it easier to maintain reliable Kafka infrastructure. The project is container-ready and integrates seamlessly with Docker and Docker Compose.

## Features

- Periodic health checks for multiple Kafka brokers
- Configurable via YAML or environment variables
- Docker and Docker Compose support
- Simple, extensible codebase

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Docker (optional, for containerized deployment)
- Docker Compose (optional, for multi-container orchestration)

## Installation

### 1. Clone the Repository

```sh
git clone https://github.com/yourusername/KafkaMonitoring.git
cd KafkaMonitoring
```

### 2. Build the Application

```sh
mvn clean package
```

This will generate a runnable JAR file in the `target/` directory.

### 3. Configuration

Edit `src/main/resources/application-local.yml` to set your Kafka bootstrap servers and other properties:

```yaml
spring:
  kafka:
    bootstrap-servers: kafka-1:29092,kafka-2:29093
```

You can also override configuration using environment variables, e.g. `SPRING_KAFKA_BOOTSTRAP_SERVERS`.

## Usage

### Running Locally

```sh
java -jar target/kafkamonitoring-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

### Running with Docker

Build the Docker image:

```sh
docker build -t kafka-monitoring-app .
```

Run the container:

```sh
docker run -e SPRING_PROFILES_ACTIVE=local -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka-1:29092,kafka-2:29093 -p 8080:8080 kafka-monitoring-app
```

### Running with Docker Compose

The included `docker-compose.yml` starts Kafka brokers and the monitoring application:

```sh
docker-compose up --build
```

## Project Structure

```
src/
  main/
    java/com/kafkamonitoring/
      KafkamonitoringApplication.java
      service/KafkaHealthService.java
      helper/KafkaAdminHelper.java
    resources/
      application-local.yml
Dockerfile
docker-compose.yml
pom.xml
```

## Customization

- **Health Check Interval:**  
  Set via `kafka.health.check.interval` in your YAML or as an environment variable.
- **Kafka Brokers:**  
  Set `spring.kafka.bootstrap-servers` in your YAML or as an environment variable.

## License

This project is licensed under the MIT License.  
See the [LICENSE](LICENSE.md) file for details.

---

**Maintainer:** [Marcos Hidalgo Nunes](mailto:tecnico@marcos.nunes.nom.br)