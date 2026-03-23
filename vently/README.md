# Vently Backend

Spring Boot REST API for the Vently event volunteer platform.

## Requirements

- Java 17
- Maven 3.8+
- PostgreSQL 14+

## Setup

1. Copy the example env file and fill in your values:
```bash
cp env.example .env
```

2. Key variables to set in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/vently
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password

aws.access.key.id=YOUR_AWS_ACCESS_KEY
aws.secret.access.key=YOUR_AWS_SECRET_KEY
aws.region=us-east-1
aws.s3.bucket=vently-profile-pictures

jwt.secret=your_jwt_secret_key
```

3. Run locally:
```bash
./mvnw spring-boot:run
```

4. Build JAR for production:
```bash
./mvnw clean package -DskipTests
```

## API Base URL

```
http://localhost:8080/api/v1
```

## Key Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/auth/register` | Register new user |
| POST | `/auth/login` | Login, returns JWT |
| GET | `/events` | List all events |
| POST | `/events` | Create event (ORGANIZER) |
| PUT | `/events/{id}` | Update event |
| POST | `/events/{id}/image` | Upload event image to S3 |
| POST | `/applications` | Apply to event (VOLUNTEER) |
| PUT | `/applications/{id}/status` | Approve/reject application |
| POST | `/attendance/mark` | Mark attendance |
| GET | `/admin/analytics` | Platform analytics (ADMIN) |

## Project Structure

```
src/main/java/com/example/vently/
├── admin/          # Admin controller, service, analytics
├── application/    # Volunteer applications
├── attendance/     # Attendance tracking, QR codes
├── audit/          # Audit logging
├── auth/           # JWT auth, registration, login
├── config/         # Security, CORS, S3, rate limiting
├── event/          # Event CRUD
├── notification/   # Email/SMS via AWS SES/SNS
├── payment/        # UPI payment handling
├── subscription/   # Subscription plans
└── user/           # User profiles
```

## Database Migrations

Flyway manages schema migrations automatically on startup. Migration files are in:
```
src/main/resources/db/migration/
```

## Production Deployment

The app runs as a systemd service on EC2:
```bash
sudo systemctl start vently
sudo systemctl status vently
sudo journalctl -u vently -f   # view logs
```

See [DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md) for full deployment steps.
