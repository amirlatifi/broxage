# Broxage - Brokerage Firm Application

## Created by Amir Latifi

Welcome to Broxage, a robust brokerage firm application designed to manage customer assets, handle orders, and provide a secure platform for financial transactions.

## Overview

Broxage is a Spring Boot-based application that offers a comprehensive solution for brokerage firms. It provides functionality for customer management, asset tracking, order processing, and secure authentication.

## Features

- Customer registration and management
- Asset tracking and management
- Order creation, listing, and cancellation
- Secure authentication and authorization
- Admin functionality for managing customer assets

## Technology Stack

- Java 11
- Spring Boot 2.5.x
- Spring Security
- Spring Data JPA
- H2 Database (for development and testing)
- Docker and Docker Compose for containerization

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven
- Docker and Docker Compose

### Running the Application

1. Clone the repository:
   ```
   git clone https://github.com/amirlatifi/broxage.git
   cd broxage
   ```

2. Build the project:
   ```
   ./mvnw clean package
   ```

3. Start the application using Docker Compose:
   ```
   docker-compose up -d
   ```

The application will be available at `http://localhost:8080`.

## API Endpoints

- POST /api/customers/register - Register a new customer
- POST /api/customers/login - Login and receive JWT token
- POST /api/orders - Create a new order
- GET /api/orders - List orders for a customer
- DELETE /api/orders/{orderId} - Cancel an order
- POST /api/assets/deposit - Deposit money
- POST /api/assets/withdraw - Withdraw money
- GET /api/assets - List assets for a customer

## Testing

To run the tests:

```
./mvnw test
```

## Contributing

As this is a personal project, please contact me directly if you have any suggestions or would like to contribute.

## License

This project is licensed under the MIT License - see the LICENSE.md file for details.

## Contact

Project Link: [https://github.com/amirlatifi/broxage](https://github.com/amirlatifi/broxage)