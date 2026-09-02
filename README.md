# QGuard Demo Scheduler

A production-oriented demo scheduling platform for QGuard. It provides a complete flow for visitors to schedule, manage, reschedule, and cancel 30-minute product demo appointments.

The project uses a Spring Boot backend, React/Vite frontend, PostgreSQL persistence, Flyway migrations, timezone-aware scheduling, secure booking management tokens, email notifications, and ICS calendar invitations.

## Features

- Schedule 30-minute QGuard demo sessions
- View available and already-booked time slots
- Prevent double-booking of the same slot
- Timezone-aware availability using IANA timezone identifiers
- Store schedule timestamps as UTC instants
- Configurable business hours
- Booking confirmation emails
- ICS calendar invitations
- Secure booking management links
- Cancel and reschedule existing bookings
- PostgreSQL persistence with Flyway migrations
- REST API with validation and centralized error handling
- Docker Compose support for local development

## Booking Flow

1. Visitor opens the QGuard demo scheduler.
2. Visitor enters contact and company details.
3. Available 30-minute slots are displayed in the visitor's timezone.
4. Already-booked slots are clearly marked as booked.
5. Visitor selects an available slot and confirms the booking.
6. The backend validates the slot and prevents concurrent double-booking.
7. A confirmation email and ICS calendar invitation are sent.
8. The visitor can use the secure management link to view, reschedule, or cancel the booking.

## Technology Stack

### Backend

- Java 21
- Spring Boot
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway
- Maven

### Frontend

- React
- TypeScript
- Vite
- Axios

### Infrastructure

- Docker / Docker Compose
- PostgreSQL
- SMTP email
- ICS calendar invitations

## Run Locally

### 1. Configure environment variables

Copy the example environment file:

```bash
cp .env.example .env