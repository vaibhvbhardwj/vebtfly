# Vently — Event Volunteer Platform

Vently is a full-stack platform that connects event organizers with volunteers. Organizers can create and manage events, while volunteers can discover opportunities, apply, and get paid for their work.

## Project Structure

```
deliverables/
├── vently/          # Spring Boot backend (Java 17)
└── vently-ui/       # React frontend (Vite + Tailwind)
```

## Tech Stack

| Layer     | Technology                                      |
|-----------|-------------------------------------------------|
| Backend   | Spring Boot 4, Java 17, PostgreSQL, Flyway      |
| Auth      | JWT (jjwt), Spring Security                     |
| Storage   | AWS S3 (`vently-profile-pictures`)              |
| Notifications | AWS SES (email), AWS SNS (SMS OTP)          |
| Frontend  | React 19, Vite 7, Tailwind CSS 4, Zustand       |
| Hosting   | AWS EC2 (Ubuntu 24.04), Nginx, Let's Encrypt    |

## Live URLs

- Frontend: https://knomochat.online
- API: https://api.knomochat.online/api/v1

## Quick Start

See individual READMEs:
- [Backend (vently/README.md)](./vently/README.md)
- [Frontend (vently-ui/README.md)](./vently-ui/README.md)

## Roles

- **ADMIN** — platform management, analytics, user moderation
- **ORGANIZER** — create/manage events, approve volunteers, mark attendance
- **VOLUNTEER** — browse events, apply, track applications and earnings

## Key Features

- JWT-based authentication with phone OTP verification
- Event creation with S3 image upload
- Volunteer application workflow (apply → approve/reject → attend)
- Attendance tracking with QR/code-based check-in
- UPI payment per volunteer
- Subscription plans (free tier: 3 events / 5 applications)
- Admin dashboard with platform analytics
- Dispute management and no-show tracking
