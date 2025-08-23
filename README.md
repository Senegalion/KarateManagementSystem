# Karate Management System

## 📌 Project Description

The **Karate Management System** is a comprehensive web application designed to help karate instructors (senseis) efficiently manage their training programs. The system is designed with both administrators and users in mind, offering an intuitive interface for managing students, payments, and communication.

With this application, administrators can manage user registrations, track payments, and automate recurring tasks such as sending overdue payment reminders. The platform integrates seamlessly with the **PayPal API** to handle payments, allowing users to pay their fees online through a secure and well-known payment gateway.

The **Karate Management System** automates key administrative tasks, saving time for instructors and improving the overall user experience. The system can automatically send email notifications about payment due dates, ensuring that students stay informed about their financial obligations.

The app provides robust tools for instructors, such as:
- Managing a list of registered users
- Assigning payments to users
- Scheduling tasks (such as sending reminders)
- Viewing payment statuses and sending reminders
- Access to a Swagger-generated API for easy integration and testing

This project aims to modernize the management of karate schools and improve communication between instructors and students, with a strong focus on automation, ease of use, and scalability.

## 🏗️ System Architecture

The project follows a Spring Cloud microservices architecture with centralized configuration, service discovery, and API routing.

🔑 Core Components
Config Server – centralized configuration management for all services,
Eureka Service Registry – service discovery and load balancing,
API Gateway (Spring Cloud Gateway) – single entry point for routing requests, handling authentication (JWT), and request filtering.

🧩 Microservices

- **auth-service** – authentication & authorization (JWT-based security, role management),
- **user-service** – user management (profiles, registration, contact information),
- **club-service** – club management (structure, assigning users to clubs),
- **training-service** – training sessions scheduling, attendance tracking,
- **feedback-service** – feedback collection and communication between students and instructors.

This architecture ensures:
- Loose coupling between services,
- Scalability (multiple instances per service),
- Centralized configuration and discovery,
- Secure and controlled access to backend services.

## 📷 Application Preview

### 🖥️ Dashboard View

Below is a screenshot of the main admin dashboard of the application:
![Dashboard View](https://github.com/user-attachments/assets/32298e6e-a42c-468c-8f66-8ebcd27c7d6c)

---

### 🚪 API Gateway (Spring Cloud Gateway)

To manage and route incoming traffic efficiently across the microservices, the system includes an **API Gateway** built using **Spring Cloud Gateway**.

This component serves as a single entry point to the system, enabling:
- Centralized routing to `user-service`, `club-service`, etc.
- Load balancing and fault tolerance
- Security handling (JWT filtering at the gateway level)
- Path rewriting and filtering of requests

This simplifies communication with the backend services and improves scalability and maintainability.

---

### 🧭 Eureka Service Registry (Service Discovery)

The system uses **Spring Cloud Eureka** as a service registry. All microservices such as `user-service` and `club-service` register themselves with Eureka, allowing them to communicate using logical service names via Feign Clients.

This setup enables:
- Easy service discovery
- Load balancing between multiple instances
- Decoupled and scalable architecture

Below is a screenshot of the **Eureka Dashboard** with multiple registered instances (e.g., two `user-service` instances):

![Eureka Service Registry](https://github.com/user-attachments/assets/2f78f5cd-8521-4e9a-931d-5699edc1b053)


---

### 🛠️ Jenkins CI/CD

This screenshot shows the Jenkins pipeline and build status:
![Jenkins Screenshot](https://github.com/user-attachments/assets/a2b2e2a8-c8c6-4743-a84f-8009a4dc8181)

---

### 🐳 Docker

Currently running Docker containers related to the project:

![Docker Containers](https://github.com/user-attachments/assets/69fe19a1-3774-4530-9163-f5a7748fc7b7)

## ✨ Features

- ✅ User registration and authentication (JWT authentication)
- ✅ Payment processing via PayPal
- ✅ Automatic email notifications for overdue payments
- ✅ Admin panel for managing users and payments
- ✅ Task scheduling for automation (Spring Scheduler)
- ✅ API documentation available via Swagger (testable endpoints)

## 🛠️ Technologies

- **Backend:** Java, Spring Boot, Spring Security, Spring Data JPA
- **Database:** PostgreSQL, Flyway
- **Payments:** PayPal API (Sandbox mode)
- **Email Handling:** Spring Mail, GreenMail (for testing)
- **Testing:** JUnit, Awaitility, Testcontainers

## 🚀 How to Run the Project

### 1️⃣ Requirements

- Java 17+
- Gradle
- Docker

### 2️⃣ Configuration
Make sure you have set the environment variables for the critical configurations (or defaults will be used):

```env
PAYPAL_CLIENT_ID=ATsjDqjpPBHA5ZNFAm4YGLPioWd6e2deYB12kbksjVD5xDROAq0QFIPf32lR5n-3_m4GcenSPsJ1dS_A
PAYPAL_CLIENT_SECRET=EHsOc_t4LQ4tndkT6iuufi6mWI44buCHZVvhwjZPalykt4XZajmUZhg5JthcFP260iLsrLQQMho9N84g
```

If you don't have a PayPal sandbox account yet, please follow the Testing PayPal Payments section below for details.

### 3️⃣ Running the Application

To run the application, follow these steps:

```sh
git clone https://github.com/Senegalion/KarateManagementSystem.git
cd karate-management-system
mvn clean install
mvn spring-boot:run
```

The application will be available at http://localhost:8080.

## 📬 Testing PayPal Payments

To test PayPal payments, you will need to create your own PayPal Sandbox account. Follow these steps:

1. Go to the PayPal Developer Portal: [PayPal Developer](https://developer.paypal.com/).
2. Sign up or log in to your PayPal account.
3. Create a new Sandbox account by navigating to **Sandbox > Accounts**.
4. Use the "Business" account type to simulate transactions.
5. Copy the Client ID and Client Secret for your sandbox application.

Once you’ve set up your PayPal Sandbox account, replace the default client-id and client-secret in the `application.properties` file or set them as environment variables in your local environment:

```env
PAYPAL_CLIENT_ID=YourPayPalSandboxClientID
PAYPAL_CLIENT_SECRET=YourPayPalSandboxClientSecret
```

**Example Sandbox Account (for testing purposes):**
- **URL:** [PayPal Sandbox](https://www.sandbox.paypal.com/)
- **Email:** sb-your-email@personal.example.com (replace with your own sandbox email)
- **Password:** YourTestPasswordHere (replace with your own sandbox password)

**Note:** Do not share your actual PayPal credentials publicly. It’s recommended to use your own sandbox account for security purposes.

## 📖 API Documentation (Swagger)

Once the application is running, API documentation is available at:

- [Swagger UI](http://localhost:8080/swagger-ui.html)

## 🛠 Author

Project created by **Łukasz Pelikan**.
