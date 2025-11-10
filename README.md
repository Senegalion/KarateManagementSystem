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
- **enrollment-service** – user enrollments for training sessions (sign-up, withdrawal, listing user/training enrollments),
- **feedback-service** – feedback collection and communication between students and instructors.
- **notification-service** – event-driven notifications (enrollment, withdrawal, registration) delivered via e-mail.

This architecture ensures:
- Loose coupling between services,
- Scalability (multiple instances per service),
- Centralized configuration and discovery,
- Secure and controlled access to backend services.

## 📷 Application Preview

### 🖥️ Dashboard View

![Main Page View](https://github.com/user-attachments/assets/f02fb80e-4ab2-48ab-a85f-835491a19a66)
![Main Page View](https://github.com/user-attachments/assets/c137bec9-2667-4eca-a129-aa9dde6fc809)
![Main Page View](https://github.com/user-attachments/assets/9f69e539-852b-4639-ad3e-dd2957d8e0cc)



Below is a screenshot of the main admin dashboard of the application:
![Dashboard View](https://github.com/user-attachments/assets/fec20397-becf-4026-b6cc-85bdbd7fc6b1)
![Dashboard View](https://github.com/user-attachments/assets/39f2006d-6440-495c-bf2b-39ca6557c8cd)
![Dashboard View](https://github.com/user-attachments/assets/33545b84-51bb-4451-b72e-a9e2b72653c2)
![Dashboard View](https://github.com/user-attachments/assets/82eb1c68-bb83-4e9b-b04b-b0277aeb7b88)
![Dashboard View](https://github.com/user-attachments/assets/a51d1244-dfd8-46e5-8f5e-375050b7fab8)
![Dashboard View](https://github.com/user-attachments/assets/c162d8f1-4c10-473f-b675-93376d0adf0f)



---

### 🚪 API Gateway (Spring Cloud Gateway)

To manage and route incoming traffic efficiently across the microservices, the system includes an **API Gateway** built using **Spring Cloud Gateway**.

This component serves as a single entry point to the system, enabling:
- Centralized routing to `user-service`, `auth-service`, `club-service`, `training-service`, `enrollment-service`, `feedback-service` and `payment-service`.
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

![Eureka Service Registry](https://github.com/user-attachments/assets/9f8fe226-09d0-4443-90a0-290f1bac4c4f)

---

### ⚡ Asynchronous Communication with Apache Kafka

The **Karate Management System** uses **Apache Kafka** as an asynchronous message broker that enables event-driven communication between microservices.  
Instead of using direct REST calls, the services publish and consume **domain events**, which allows for loose coupling, scalability, and fault tolerance.

Kafka ensures that services can operate independently — for example, the `user-service` can emit events without waiting for the `notification-service` or `payment-service` to respond in real time.

#### 🔄 Event-Driven Flow Overview

1. **User Registration Event**  
   When a new user registers, the `user-service` publishes a `USER_REGISTERED` event to the Kafka topic `user-events`.  
   The `notification-service` consumes this event and sends a **welcome email** to the newly registered user.

2. **User Deletion Event**  
   When a user account is deleted, the `user-service` emits a `USER_DELETED` event to the topic `user.deleted`.  
   The `payment-service` consumes this event and automatically removes all related payment records and user snapshots.

3. **Training Enrollment Event**  
   When a user enrolls in a training session, the `enrollment-service` publishes an `ENROLLMENT_CREATED` event.  
   The `notification-service` listens to this topic and sends a **confirmation email** containing training details.

4. **Feedback Event**  
   After each training, a `FEEDBACK_CREATED` event is produced by the `feedback-service`.  
   This triggers the `notification-service` to send a **thank-you email** confirming that the feedback has been received.

5. **Payment Received Event**  
   When the `payment-service` captures a PayPal transaction, it publishes a `PAYMENT_RECEIVED` event.  
   The `notification-service` reacts to this event by sending a **payment confirmation email** to the user.

#### 🧩 Kafka Topics

| Topic name | Producer Service | Consumer Service(s) | Event Type | Description |
|-------------|------------------|----------------------|-------------|--------------|
| `user-events` | user-service | notification-service | `USER_REGISTERED` | Triggered when a new user is registered |
| `user.deleted` | user-service | payment-service | `USER_DELETED` | Removes user-related data from payment DB |
| `training.enrollment` | enrollment-service | notification-service | `ENROLLMENT_CREATED` | Confirms user’s training enrollment |
| `feedback.events` | feedback-service | notification-service | `FEEDBACK_CREATED` | Sends a thank-you message for feedback |
| `payment.events` | payment-service | notification-service | `PAYMENT_RECEIVED` | Confirms successful PayPal payment |

#### 📬 Email Notification System (Kafka-driven)

The **notification-service** acts as a central Kafka consumer responsible for processing domain events and generating email messages.  
It uses **Spring Kafka** and **HTML templates** to render multilingual email content.

Emails are sent automatically in response to specific Kafka events:

| Event | Triggering Service | Email Type |
|--------|-------------------|-------------|
| `USER_REGISTERED` | user-service | Welcome email |
| `ENROLLMENT_CREATED` | enrollment-service | Enrollment confirmation |
| `FEEDBACK_CREATED` | feedback-service | Thank-you email |
| `PAYMENT_RECEIVED` | payment-service | Payment confirmation |

This approach makes the system **reactive and scalable** — adding a new type of notification only requires publishing a new event type, without modifying existing services.

#### ⚙️ Example (User Registration Event)
```java
// user-service
publisher.publishUserRegistered(
    new UserRegisteredEvent(
        UUID.randomUUID().toString(),
        "USER_REGISTERED",
        Instant.now(),
        new UserRegisteredEvent.Payload(
            user.getId(),
            user.getEmail(),
            user.getUsername(),
            user.getClubId(),
            user.getClubName(),
            user.getKarateRank(),
            user.getRegistrationDate()
        )
    )
);
```

```java
// notification-service
@KafkaListener(topics = "user-events", groupId = "notification-service")
public void onUserRegistered(UserRegisteredEvent event) {
    String to = event.getPayload().getUserEmail();
    String body = template.render("email/user-registered.html", Map.of(
        "username", event.getPayload().getUsername(),
        "clubName", event.getPayload().getClubName()
    ));
    emailService.sendHtml(to, "Welcome to Karate Management System", body);
}
```

### ✅ Advantages of Kafka in this System

- `Decoupled architecture`: services do not depend on synchronous HTTP calls.
- `Resilience`: temporary downtime of one service doesn’t affect others.
- `Scalability`: new consumers can subscribe to existing events easily.
- `Extensibility`: adding new event types doesn’t require refactoring existing code.
- `Auditability`: all domain events can be stored and replayed for debugging or analytics.

### 🧠 Summary

Using Apache Kafka allows the Karate Management System to implement a robust event-driven architecture.
This ensures reliable asynchronous communication between microservices such as user-service, payment-service, and notification-service.
The result is a scalable, fault-tolerant, and modern web application capable of automating communication and enhancing user experience.

---

## 🛠️ Jenkins CI/CD

**Jenkins** serves as the core of the Continuous Integration / Continuous Delivery (CI/CD) process for the **Karate Management System**.  
It fully automates the building, testing, and promotion of code changes across **development (dev)**, **quality assurance (test)**, and **production-ready (master)** environments.

The pipeline is configured as a **Multibranch Pipeline**, which continuously monitors the repository and automatically triggers the process for incoming merges.

---

![Jenkins Screenshot](https://github.com/user-attachments/assets/a01eda72-1ccf-4338-a1dd-5848ea225e9c)


## 🌳 Branching Strategy

The project utilizes a simplified **GitFlow** approach to ensure a stable progression of code quality through controlled environments:

- **Feature/Fix Branches:** Start from `dev`. They are merged into `dev` via Pull Requests (PRs).  
- **`dev` (Development/Staging):** The initial integration environment. Successful tests automatically promote the code to `test`.  
- **`test` (Quality Assurance/E2E):** The dedicated QA environment where the full suite of tests (including integration/E2E) is executed. Successful completion automatically promotes the code to `master`.  
- **`master` (Production-Ready):** The stable branch that reflects the production state.

---

## ⚙️ Pipeline Stages (Jenkinsfile)

The pipeline logic, defined in the **declarative Jenkinsfile**, is **conditional**, ensuring only necessary steps are executed based on the branch and detected changes:

| **Stage** | **Trigger Condition** | **Function** |
|------------|----------------------|---------------|
| **Detect changed services** | Always | Uses `ci/changed-services.sh` to identify modified microservices for focused, faster builds. |
| **Unit tests (changed)** | Branch `dev` & changes detected | Runs parallel unit tests only on affected microservices. |
| **Build & push images (changed)** | Branch `dev` & changes detected | Builds and pushes Docker images only for updated services. |
| **Promote dev → test** | Branch `dev` | Automatic merge using secured `github-token`. |
| **Full test suite on test** | Branch `test` | Executes full test suite across all microservices (unit + integration). |
| **Promote test → master** | Branch `test` | Automatic merge from `test` into `master` after successful tests. |
| **Master: build & push all images** | Branch `master` *(Currently Disabled)* | Reserved for production image tagging and deployment. |

---

## 🔑 Security & Credentials

Sensitive operations are securely managed using **Jenkins Credentials**:

- **`github-token` (Secret Text):** Used for authenticated Git operations (dev → test and test → master promotions).  
- **`docker-registry` (Username/Password):** Used to log into Docker Registry (`docker.io`) and push built images.

---

## 🟢 Status

The **CI/CD pipeline** is fully functional — all primary branches are **Green** — providing a **robust, automated, and reliable** delivery process from development commit to production-ready code.

---

### 🐳 Docker

Currently running Docker containers related to the project:

![Docker Containers](https://github.com/user-attachments/assets/75079697-d0b8-410e-a129-2d94da039b87)

## ✨ Features

- ✅ User registration and authentication (JWT authentication)
- ✅ Payment processing via PayPal
- ✅ Automatic email notifications for overdue payments
- ✅ Admin panel for managing users and payments
- ✅ Task scheduling for automation (Spring Scheduler)
- ✅ API documentation available via Swagger (testable endpoints)

## 🛠️ Technologies

- **Backend:** Java, Kotlin, Spring Boot, Spring Cloud, Spring Security, Spring Data JPA
- **Database:** PostgreSQL, Redis, Flyway
- **Payments:** PayPal API (Sandbox mode)
- **Email Handling:** Spring Mail, GreenMail (for testing)
- **Testing:** JUnit, Awaitility, Testcontainers

## 🚀 How to Run the Project

### 1️⃣ Requirements

- Java 17+
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
