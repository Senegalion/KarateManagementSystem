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
git clone https://github.com/twoj-repo/karate-management-system.git
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