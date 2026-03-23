# Vently Setup Guide

## Prerequisites

1. **Java 17+** - Verify with `java -version`
2. **Maven 3.6+** - Verify with `mvn -version`
3. **PostgreSQL 12+** - Running on localhost:5432
4. **Node.js 18+** (for frontend) - Verify with `node -version`

## Database Setup

### 1. Create Database
```sql
CREATE DATABASE vently;
```

### 2. Apply Migrations
The migrations should be applied automatically when you start the application. However, if you encounter issues:

```bash
# Run migrations manually
./mvnw flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/vently -Dflyway.user=postgres -Dflyway.password=your-password
```

### 3. Manual V1 Migration (If Needed)
If the V1 migration was skipped during baseline, you need to manually apply it:

**Using psql:**
```bash
psql -U postgres -d vently -f src/main/resources/db/migration/V1__extend_user_table.sql
```

**Using pgAdmin or DBeaver:**
1. Connect to the `vently` database
2. Open and execute `src/main/resources/db/migration/V1__extend_user_table.sql`

## Environment Configuration

### 1. Copy Environment Template
```bash
cp .env.example .env
```

### 2. Configure AWS S3 (Required)

**Why needed:** Profile pictures, Excel attendance files, dispute evidence

**Setup steps:**
1. Create AWS account at https://aws.amazon.com
2. Go to IAM → Users → Create User
3. Attach policy: `AmazonS3FullAccess`
4. Create access key → Save credentials
5. Go to S3 → Create bucket named `vently-profile-pictures`
6. Update `.env` with your credentials:
   ```
   AWS_ACCESS_KEY=AKIA...
   AWS_SECRET_KEY=wJalr...
   AWS_REGION=us-east-1
   AWS_S3_BUCKET=vently-profile-pictures
   ```

**Alternative for Development:** Use LocalStack or MinIO for local S3 simulation

### 3. Configure Razorpay (Required)

**Why needed:** Escrow payments, volunteer payouts, subscriptions

**Setup steps:**
1. Sign up at https://razorpay.com
2. Go to Settings → API Keys
3. Copy "Key ID" and "Key Secret" (test mode)
4. Update `.env`:
   ```
   RAZORPAY_KEY_ID=rzp_test_...
   RAZORPAY_KEY_SECRET=rzp_test_...
   ```

**Test Mode:** Use test API keys for development. No real money is charged.

### 4. Configure AWS SES (Optional - for later)

**Why needed:** Email notifications (welcome, verification, status updates)

**Setup steps:**
1. Go to AWS SES console
2. Verify your sender email address
3. Request production access (if needed)
4. Update `.env`:
   ```
   AWS_SES_REGION=us-east-1
   AWS_SES_FROM_EMAIL=noreply@yourdomain.com
   ```

**Alternative for Development:** Use MailHog or Mailpit for local email testing

## Running the Application

### Backend (Spring Boot)

```bash
# Navigate to backend directory
cd vently

# Run with Maven
./mvnw spring-boot:run

# Or build and run JAR
./mvnw clean package
java -jar target/vently-0.0.1-SNAPSHOT.jar
```

The backend will start on http://localhost:8080

### Frontend (React + Vite)

```bash
# Navigate to frontend directory
cd vently-ui

# Install dependencies
npm install

# Run development server
npm run dev
```

The frontend will start on http://localhost:5173

## Testing

### Run All Tests
```bash
./mvnw test
```

### Run Specific Test Class
```bash
./mvnw test -Dtest=EventServiceTest
```

### Run with Coverage
```bash
./mvnw test jacoco:report
```

## Common Issues

### Issue: "Missing column [account_status]"
**Solution:** Manually apply V1 migration (see Database Setup section)

### Issue: "AWS credentials not found"
**Solution:** 
- Set environment variables: `export AWS_ACCESS_KEY=...`
- Or add to application.properties (not recommended for production)
- Or use AWS CLI: `aws configure`

### Issue: "Razorpay API key invalid"
**Solution:**
- Verify you're using the correct key (test vs live)
- Check for extra spaces or quotes
- Ensure key starts with `rzp_test_` or `rzp_live_`

### Issue: "Connection refused to PostgreSQL"
**Solution:**
- Verify PostgreSQL is running: `pg_isready`
- Check connection details in application.properties
- Ensure database `vently` exists

## Development Workflow

1. **Start PostgreSQL** (if not running)
2. **Set environment variables** or create `.env` file
3. **Run backend**: `./mvnw spring-boot:run`
4. **Run frontend** (in separate terminal): `cd vently-ui && npm run dev`
5. **Access application**: http://localhost:5173

## Production Deployment

### Environment Variables
Set these in your production environment (AWS, Heroku, etc.):
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `AWS_ACCESS_KEY`
- `AWS_SECRET_KEY`
- `AWS_REGION`
- `AWS_S3_BUCKET`
- `RAZORPAY_KEY_ID` (use live key: `rzp_live_...`)
- `RAZORPAY_KEY_SECRET` (use live secret)

### Database
- Use managed PostgreSQL (AWS RDS, Heroku Postgres, etc.)
- Run migrations on deployment
- Enable SSL connections

### Security
- Use secrets management (AWS Secrets Manager, HashiCorp Vault)
- Enable HTTPS
- Set strong JWT secret
- Rotate credentials regularly

## Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [AWS S3 Documentation](https://docs.aws.amazon.com/s3/)
- [Razorpay API Documentation](https://razorpay.com/docs/api)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
