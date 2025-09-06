# Task Card App - Backend

## Requirements
- Java JDK
- Maven
- Docker to run docker compose with all services (see readme in the repository of the frontend)

## Initial Setup

Create a .env file to set your credentials for your database connection and for initialization of the admin account.
Change your admin password after your first log in.

```
MYSQL_USER=root
MYSQL_PASSWORD=Geheim01
MYSQL_ROOT_PASSWORD=Geheim02
APP_ADMIN_EMAIL=admin@example.com
APP_ADMIN_PASSWORD=12345678
```

## Goals of this project
- Didactical analysis of Spring Boot framework for ...
  - Layers of a modern Web Architecture
  - ORM with JPA
  - Repositories
  - Services
  - RestControllers
  - Authentication with JWT
  - User role assignment