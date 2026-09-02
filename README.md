# QGuard Demo Scheduler

A production-oriented demo scheduling platform built for **QGuard by TrevasQ**.

The platform allows visitors to schedule a 30-minute product demonstration, view slot availability in their timezone, receive confirmation emails with calendar invitations, and securely manage their bookings through rescheduling or cancellation.

The application is implemented as a modular monolith with a Spring Boot REST API, React frontend, PostgreSQL persistence, Flyway migrations, timezone-aware scheduling, secure booking management tokens, email notifications, and transactional booking protection.

---

## ✨ Features

### Scheduling

- 30-minute demo slots
- Configurable business working hours
- Timezone-aware availability
- Automatic detection of the visitor's timezone
- Ability to select/change timezone
- Future slots displayed with clear availability status
- Already-booked slots displayed separately

### Booking

- Visitor details:
  - Name
  - Email
  - Company
  - Job title
  - Phone number (optional)
- Server-side request validation
- Transactional booking creation
- Database-level protection against double-booking
- Unique slot identification

### Booking Management

- Secure management URL for every booking
- View booking details
- Reschedule an existing booking
- Cancel an existing booking
- Management tokens are cryptographically generated
- Only SHA-256 token hashes are persisted

### Notifications

- Booking confirmation email
- Booking management link
- ICS calendar invitation
- SMTP support
- Gmail SMTP support for real email delivery
- Mailpit support for local development

### API

- RESTful Spring Boot API
- Validation and centralized error handling
- Swagger/OpenAPI documentation
- PostgreSQL persistence
- Flyway database migrations

---

## 🔄 Booking Flow

```text
Landing Page
     │
     ▼
Schedule Demo
     │
     ▼
Enter Visitor Details
     │
     ▼
Select Date & Timezone
     │
     ▼
View Available / Booked Slots
     │
     ▼
Select Available 30-Minute Slot
     │
     ▼
Confirm Booking
     │
     ├──────────────► Confirmation Email
     │
     ├──────────────► ICS Calendar Invitation
     │
     ▼
Secure Management Link
     │
     ├──────────────► View Booking
     ├──────────────► Reschedule
     └──────────────► Cancel
