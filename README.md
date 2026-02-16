# Problem Solutions Ltd. | Enterprise Inventory Engine

> **Status:** 🚧 In Active Development

An enterprise-grade RESTful API architected to manage high-tech inventory, track real-time stock levels, and enforce automated business logic for specialized supply chains. 

This system serves as the foundational backend for a comprehensive Warehouse Management System (WMS), ensuring data integrity, strict validation, and seamless scalability.

## 🚀 Tech Stack
* **Core:** Java 25, Spring Boot 4
* **Data Layer:** Spring Data JPA, Hibernate, PostgreSQL
* **Infrastructure:** Docker, Docker Compose
* **Tools:** Maven, Lombok, Postman
* **Frontend (Upcoming):** React.js

## 🏗 Architecture & Design Principles
This application strictly adheres to industry-standard enterprise patterns:
* **N-Tier Architecture:** Clean separation of concerns using the **Controller-Service-Repository** pattern. Controllers strictly route HTTP traffic, while Services handle 100% of the complex business logic.
* **Automated State Management:** Business rules automatically dictate item availability. (e.g., Inventory dropping to `0` automatically triggers an `OUT_OF_STOCK` status update).
* **Data Integrity:** Implementation of `jakarta.validation` (`@NotBlank`, `@Min`) ensures malformed data is rejected before reaching the persistence layer.
* **Centralized Exception Handling:** Custom exception mapping (`ResponseStatusException`) guarantees clients receive standardized, actionable HTTP error responses (e.g., cleanly handling `404 Not Found` or `400 Bad Request`).

## 🛠 Core Features (Current)
* **Inventory Initialization:** Register new products with defined categories, pricing, and initial stock quantities.
* **Stock Retrieval:** Fetch entire inventory catalogs or query specific items by ID.
* **Automated Lifecycle Validation:** System automatically categorizes and updates item statuses based on predefined logic upon creation.

## 🚀 Getting Started

### Prerequisites
* [Docker Desktop](https://www.docker.com/products/docker-desktop/) (Running)
* [Java 25](https://adoptium.net/)

### Local Deployment
1. **Clone the repository:**
   
     ```git clone https://github.com/AlaricAbraham/ProblemSolutionsLtd.git```
   
2. **Spin up the PostgreSQL Database:**
   
     ```docker-compose up -d```
   
3. **Run the Spring Boot Application:**
   
    ```./mvnw spring-boot:run```
   
The API will boot up and bind to `http://localhost:8080`


## 📡 Example API Usage

Get all items

```GET /api/v1/solutions```


Get item by id (e.g: id is "2")

```GET /api/v1/solutions/2```


Create a New Inventory Item

```
POST /api/v1/solutions

{
    "name": "Kryptonite Laser",
    "description": "High-powered laser for dealing with extraterrestrial threats.",
    "category": "WEAPON", 
    "stockQuantity": 50,
    "price": 1500000.00
}
```

## 🗺 Development Roadmap

    [x] Phase 1: Spring Boot API Skeleton & Dockerized PostgreSQL Setup

    [x] Phase 2: Core Data Models, strict Validation, and Global Error Handling

    [ ] Phase 3: Full CRUD Capabilities (Update/Delete endpoints)

    [ ] Phase 4: Transactional Audit Logging (Immutable history tracking for all stock adjustments)

    [ ] Phase 5: React.js Frontend Dashboard integration for real-time visualization
