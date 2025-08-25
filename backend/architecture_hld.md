# High-Level Architecture HLD

This document describes the overall microservices architecture for the Client Survey Application.

## 1. Architectural Overview

The application is designed as a distributed system composed of five independent microservices. This architecture promotes separation of concerns, scalability, and maintainability.

### Key Components

* **API Gateway:** All client requests (from the frontend application) are routed through a single API Gateway. It is responsible for request authentication, routing to the appropriate service, and aggregating responses. This provides a unified entry point to the system.

* **Microservices:** The five core services (`Auth`, `Survey`, `Links`, `Response`, `Analytics`) each manage a specific business domain. Each service has its own dedicated database to ensure loose coupling.

* **Message Bus (Asynchronous Communication):** For decoupling services and handling asynchronous tasks, a message bus (e.g., RabbitMQ, Kafka) is used. For example, when a survey is submitted to the `Response Service`, it publishes an event like `ResponseReceived`. The `Analytics Service` subscribes to this event to process the data without creating a direct dependency on the `Response Service`.

## 2. Architecture Diagram

The following diagram illustrates the interaction between the system's components.

```mermaid
graph TD
    subgraph "Client Application (Browser)"
        Frontend
    end

    subgraph "Cloud Infrastructure"
        API_Gateway[API Gateway]

        subgraph "Synchronous Communication (REST APIs)"
            Auth_Service[Auth Service]
            Survey_Service[Survey Service]
            Links_Service[Links Service]
            Response_Service[Response Service]
            Analytics_Service_API[Analytics Service]
        end

        subgraph "Databases"
            Auth_DB[(Auth DB)]
            Survey_DB[(Survey DB)]
            Links_DB[(Links DB)]
            Response_DB[(Response DB)]
            Analytics_DB[(Analytics DB)]
        end

        subgraph "Asynchronous Communication"
            Message_Bus[Message Bus]
        end

        %% Connections
        Frontend --> API_Gateway

        API_Gateway --> Auth_Service
        API_Gateway --> Survey_Service
        API_Gateway --> Links_Service
        API_Gateway --> Response_Service
        API_Gateway --> Analytics_Service_API

        Auth_Service --- Auth_DB
        Survey_Service --- Survey_DB
        Links_Service --- Links_DB
        Response_Service --- Response_DB
        Analytics_Service_API --- Analytics_DB

        Response_Service -- Publishes 'ResponseReceived' event --> Message_Bus
        Message_Bus -- Consumes event --> Analytics_Service_Processing((Analytics Processor))
        Analytics_Service_Processing --> Analytics_DB

    end

    style Frontend fill:#f9f,stroke:#333,stroke-width:2px
    style API_Gateway fill:#bbf,stroke:#333,stroke-width:2px
```
