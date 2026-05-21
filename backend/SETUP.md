# Backend Configuration and Startup Guide

This document explains how to configure environment variables and PostgreSQL connectivity to run the EOS Spring Boot backend in **local**, **development**, and **production** environments.

---

## 1. Overview

The backend application requires:

- Environment variables for server and database configuration
- Spring Boot profile–based configuration files
- PostgreSQL as the database
- Docker (for local development database)

---

## 2. Required Environment Variables

| Variable | Description | Example |
| --- | --- | --- |
| SERVER_PORT | Application port | 8080 |
| DB_HOST | PostgreSQL host | localhost |
| DB_PORT | PostgreSQL port | 5432 |
| DB_NAME | Database name | backend |
| DB_USERNAME | Database username | postgres |
| DB_PASSWORD | Database password | postgres |
| GOOGLE_CLIENT_ID | Google OAuth 2.0 Client ID | `your-google-client-id` |
| GOOGLE_CLIENT_SECRET | Google OAuth 2.0 Client Secret | `your-google-client-secret` |
| JWT_SECRET | Secret key for signing JWT tokens | `your-secure-jwt-secret-key` |

---

## 3. Declaring Environment Variables

### Linux / macOS

```bash
export SERVER_PORT=8080
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=backend
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export GOOGLE_CLIENT_ID="your-google-client-id"
export GOOGLE_CLIENT_SECRET="your-google-client-secret"
export JWT_SECRET="your-secure-jwt-secret-key"
```

### Windows (PowerShell)

```powershell
$env:SERVER_PORT = "8080"
$env:DB_HOST = "localhost"
$env:DB_PORT = "5432"
$env:DB_NAME = "backend"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "postgres"
$env:GOOGLE_CLIENT_ID = "your-google-client-id"
$env:GOOGLE_CLIENT_SECRET = "your-google-client-secret"
$env:JWT_SECRET = "your-secure-jwt-secret-key"
```

---

## 4. Google OAuth 2.0 Configuration (Client ID & Client Secret)

This guide explains how to obtain `client_id` and `client_secret` from Google Cloud Console to integrate **"Sign in with Google"** functionality into your application.

---

### Step 1: Create a Project on Google Cloud Console

1. **Go to Google Cloud Console:** [https://console.cloud.google.com/](https://console.cloud.google.com/)

2. **Sign in with your Google account.**

3. **At the top left corner** (next to the Google Cloud logo), click on the project dropdown.

4. **Select "New Project".**

5. **Enter a project name** (e.g., `My Graduation Project`) and click **"Create"**.

6. **Wait a few seconds** for Google to create the project, then make sure to **select the newly created project** to begin configuration.

---

### Step 2: Configure OAuth Consent Screen

This is the screen that will be displayed to users when they click the "Sign in with Google" button, asking them to grant permissions to your application.

1. **In the left menu**, navigate to **"APIs & Services" > "OAuth consent screen"**.

2. **Select User Type:**
   - Choose **"External"** to allow anyone with a Google account to sign in.
   - Click **"Create"**.

3. **Fill in the required information:**
   - **App name:** Your application name (will be displayed to users).
   - **User support email:** Support email address.
   - **Developer contact information:** Your email address.

4. **For the next steps** (Scopes, Test users), you can keep the defaults and click **"Save and Continue"** until you complete the setup and return to the Dashboard.

---

### Step 3: Create Credentials

This step will generate the `client_id` and `client_secret` pair.

1. **In the left menu**, select **"Credentials"**.

2. **Click the "+ CREATE CREDENTIALS"** button at the top and select **"OAuth client ID"**.

3. **Under "Application type"**, select **"Web application"**.

4. **Enter a name for your Web client** (e.g., `Backend Server`).

5. **Security Configuration (Important):**

   - **Authorized JavaScript origins:** Enter the URL where your **Frontend** is running.
     - Example for local environment: `http://localhost:3000`
     - Example for production environment: `https://your-frontend-domain.com`

   - **Authorized redirect URIs:** Enter the **Backend** API endpoint that will handle the Authorization Code.
     - Example for local environment: `http://localhost:8080/api/auth/google/callback`
     - Or depending on your flow design: `http://localhost:3000`
     - Example for production environment: `https://your-backend-domain.com/api/auth/google/callback`

   > **Note:** If you're not sure which URL will be used, you can add multiple URLs to the list. Google will only allow redirects to URLs declared here.

6. **Click "Create".**

---

### Step 4: Save Client ID & Client Secret

After successful creation, a dialog box will appear containing the **Client ID** and **Client Secret**. Add these values to your `.env` file or configure them as environment variables as shown in Section 3 above.

---

## 5. JWT Secret Configuration

The JWT secret is used to sign and verify JWT tokens for authentication. It's critical to use a strong, random secret key to ensure the security of your application.

---

### What is JWT_SECRET?

`JWT_SECRET` is a secret key used to:
- **Sign** JWT tokens when they are created
- **Verify** JWT tokens when validating authentication requests
- Prevent token tampering and unauthorized access

---

### Security Requirements

1. **Length**: Minimum 256 bits (32 characters) recommended, preferably 512 bits (64 characters) or more
2. **Randomness**: Must be cryptographically random
3. **Uniqueness**: Use different secrets for development, staging, and production environments
4. **Confidentiality**: Never commit JWT_SECRET to version control or expose it publicly

---

### How to Generate a Secure JWT Secret

#### Option 1: Using OpenSSL (Linux / macOS / Git Bash on Windows)

Generate a 256-bit (32-byte) secret:
```bash
openssl rand -base64 32
```

Generate a 512-bit (64-byte) secret (recommended):
```bash
openssl rand -base64 64
```

#### Option 2: Using PowerShell (Windows)

Generate a 256-bit (32-byte) secret:
```powershell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Minimum 0 -Maximum 256 }))
```

Generate a 512-bit (64-byte) secret (recommended):
```powershell
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Minimum 0 -Maximum 256 }))
```

#### Option 3: Using Node.js

```bash
node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"
```

Or for 64 bytes:
```bash
node -e "console.log(require('crypto').randomBytes(64).toString('base64'))"
```

#### Option 4: Using Python

```bash
python -c "import secrets; print(secrets.token_urlsafe(32))"
```

Or for 64 bytes:
```bash
python -c "import secrets; print(secrets.token_urlsafe(64))"
```

#### Option 5: Online Generator (Use with caution)

For development purposes only, you can use online tools like:
- https://generate-secret.vercel.app/32
- https://www.browserling.com/tools/random-string

**Important:** Do **not** use online secret generators for JWT secrets or any other sensitive values. Any secrets generated online must be treated as compromised and must never be used in environments that handle real or sensitive data (including development, staging, or testing with real data). Always generate secrets locally using trusted tools like those shown above.

---

### Save JWT Secret

After successful creation, add your jwt secret value to your `.env` file or configure them as environment variables as shown in Section 3 above.

## 6. Spring Boot Configuration

### application.yaml

```yaml
spring:
  application:
    name: backend
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:backend}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
  liquibase:
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.yaml
server:
  port: ${SERVER_PORT:8080}
google:
  client-id: ${GOOGLE_CLIENT_ID}
  client-secret: ${GOOGLE_CLIENT_SECRET}
jwt:
  secret: ${JWT_SECRET}
  access-token-expiration: 900000      # 15 minutes in milliseconds
```

### application-dev.yaml

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: none
  liquibase:
    contexts: dev
```

### application-prod.yaml

```yaml
server:
  shutdown: graceful
spring:
  datasource:
    hikari:
      data-source-properties:
        sslmode: require
  jpa:
    hibernate:
      ddl-auto: validate
  liquibase:
    contexts: prod
```

### **Liquibase Configuration Notes**

**Important**: When `liquibase.enabled=true`, if no change sets are defined under `classpath:db/changelog/changes` (as referenced by `classpath:db/changelog/db.changelog-master.yaml`), the application may throw exceptions during startup or testing.

To avoid this, choose one of the following options:

**1.** Set `liquibase.enabled=false` in the configuration files.

**2.** Update `classpath:db/changelog/db.changelog-master.yaml` to contain an empty change log:

```yaml
databaseChangeLog: []
```

---

## 7. Local Development Database (Docker)

### 7.1 `docker-compose.yaml`

This configuration sets up a PostgreSQL 16 container with Alpine Linux for a lightweight, production-ready local development environment. Customize the service name, image version, and resource limits based on your project needs.

```yaml
services:
  postgres:
    image: postgres:16-alpine
    container_name: postgres-container
    environment:
      POSTGRES_DB: ${DB_NAME:-backend}
      POSTGRES_USER: ${DB_USERNAME:-postgres}
      POSTGRES_PASSWORD: ${DB_PASSWORD:-postgres}
    ports:
      - "${DB_PORT:-5432}:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test:
        [
          "CMD-SHELL",
          "pg_isready -U ${DB_USERNAME:-postgres} -d ${DB_NAME:-backend}",
        ]

volumes:
  postgres_data:
    driver: local
```

### 7.2 `.env`

Create a `.env` file in the same directory as docker-compose.yaml:

```env
DB_NAME=backend
DB_USERNAME=postgres
DB_PASSWORD=postgres
DB_PORT=5432
```

### 7.3 Run the Database Container

Make sure both .env and docker-compose.yaml are located in the same directory.

From that directory, run:

`docker compose up -d`

The PostgreSQL database will now be available at : `localhost:${DB_PORT}`

You can connect to it from the Spring Boot application using: `jdbc:postgresql://localhost:${DB_PORT}/${DB_NAME}`

---

## 8. Running the Application

### 8.1 Linux / macOS

```bash
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

```bash
SPRING_PROFILES_ACTIVE=prod java -jar app.jar
```

### 8.2 Windows (PowerShell)

```powershell
$env:SPRING_PROFILES_ACTIVE = "dev"
./mvnw spring-boot:run
```

```powershell
$env:SPRING_PROFILES_ACTIVE = "prod"
java -jar app.jar
```

---

## 9. Backend CI - Test and Quality Checks (GitHub Actions)

This section explains the Continuous Integration (CI) pipeline for the backend service. The CI pipeline acts as an automated gatekeeper, ensuring that no broken code or failing tests are merged into the main production branch.

### 9.1 How the CI Pipeline Works

Our CI workflow (`backend-ci.yml`) is fully automated via GitHub Actions and is triggered under two conditions:

1. Pull Requests: Whenever a PR is opened or updated with changes in `backend/**` or `.github/workflows/backend-ci.yml`.
2. Direct Pushes: Whenever code is pushed directly to the `main` branch with changes in `backend/**` or `.github/workflows/backend-ci.yml`.

The workflow contains two jobs:

1. `test` job (runs on PRs and pushes):
  - Provisions Ubuntu and sets up Java 21.
  - Caches Maven dependencies.
  - Ensures the Maven wrapper is executable (`chmod +x mvnw`).
  - Runs `./mvnw -B clean verify`.
  - Uses test-safe environment values:
    - `SPRING_PROFILES_ACTIVE=test`
    - `JWT_SECRET=test-jwt-secret-should-be-at-least-32-characters`
    - `GOOGLE_CLIENT_ID=test-google-client-id`
    - `GOOGLE_CLIENT_SECRET=test-google-client-secret`
  - Uploads `backend-test-reports` artifact when the job fails.

2. `publish-image` job (runs only on pushes to `main`, after `test` passes):
  - Downloads the tested JAR artifact (`backend-jar-${GITHUB_SHA}`, retained for 14 days).
  - Builds and pushes a Docker image tagged with the immutable commit SHA (`DOCKERHUB_USER/eos:${GITHUB_SHA}`).

Important: deployment is not automatic in this workflow. Release to Render is handled by a separate manual workflow described in Section 10.

### 9.2 Database Testing with Testcontainers

You do not need to provide external database credentials for the CI pipeline.

Our backend uses Testcontainers for integration testing. During the CI run, Testcontainers automatically spins up an ephemeral PostgreSQL Docker container, runs database migrations/tests against it, and tears it down afterward.

This ensures our tests are completely isolated and do not pollute any real databases.

### 9.3 Handling Test Properties

During the CI run, the application uses the test profile.

The pipeline sets `SPRING_PROFILES_ACTIVE=test`. Ensure that your `src/test/resources/application-test.yaml` contains all necessary test-safe configurations (for example cookie properties and mock authentication values) to allow the Spring Application Context to load successfully.

### 9.4 Troubleshooting CI Failures

If the CI pipeline fails (marked with a red cross on GitHub):

1. Do not merge the PR. A failed CI means the code is broken.
2. Go to the Actions tab in the GitHub repository.
3. Click on the failed workflow run.
4. Scroll down to the Artifacts section at the bottom of the summary page.
5. Download the `backend-test-reports` artifact. This contains detailed XML reports from Maven (Surefire and Failsafe) showing exactly which test cases failed and why.
6. Fix the code locally, run `./mvnw clean verify` to ensure it passes on your machine, and push a new commit to re-trigger the CI.

---

## 10. Unified CD - Frontend and Backend Deployment

### 10.1 Prepare Before Deploy

Before configuring Render, ensure your GitHub repository has the required secrets configured for the GitHub Actions pipeline:

1. `DOCKERHUB_USER`: Your Docker Hub username.
2. `DOCKERHUB_TOKEN`: A Personal Access Token from Docker Hub.
3. `RENDER_DEPLOY_HOOK`: Render Deploy Hook URL used by GitHub Actions.

Ensure `backend/Dockerfile`, `.github/workflows/backend-ci.yml`, and `.github/workflows/deploy.yml` are present in the `main` branch.

### 10.2 Create a New Service on Render

1. Sign in to Render: https://dashboard.render.com/
2. Click New, then Web Service.
3. Instead of connecting a Git repository, select Deploy an existing image from a registry.
4. In the Image URL field, enter your Docker image path:

`docker.io/YOUR_DOCKERHUB_USER/eos:latest` (replace `YOUR_DOCKERHUB_USER` with your actual username).

5. Click Next to proceed to the service settings.

### 10.3 Configure Region and Instance

1. Region: Choose a region closest to your users and/or your database region. Lower network distance reduces API latency.
2. Instance Type: Select the tier that fits your needs (Free or Starter is usually sufficient for initial deployments).

### 10.4 Configure Deploy Hook (The Bridge between GitHub and Render)

Since Render does not track your Git commits directly in this image-based setup, use a Deploy Hook to trigger updates:

1. After creating the Web Service, open the Settings tab.
2. Find the Deploy Hook section and copy the provided URL.
3. Go back to your GitHub Repository > Settings > Secrets and variables > Actions.
4. Create a new secret named `RENDER_DEPLOY_HOOK` and paste the URL.

Note: GitHub Actions uses this URL to notify Render whenever a new Docker image is successfully pushed to Docker Hub.

### 10.5 Add Environment Variables in Render

In Render, open your Web Service > Environment and add your production variables:

1. `SPRING_PROFILES_ACTIVE=prod`
2. `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
3. `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`
4. `JWT_SECRET`
5. `ALLOWED_ORIGINS`
6. `JAVA_OPTS` (optional): You can pass JVM arguments here (for example `-Xmx512m`) because the Dockerfile supports it.

Note: Do not set `SERVER_PORT` on Render. Render dynamically assigns the `PORT` variable to the container.

### 10.6 The CI/CD Pipeline Flow Explained

Our deployment process uses automated CI plus manual release approval:

1. CI Trigger (automatic): On backend-related PR and push events, `backend-ci.yml` runs tests.
2. Immutable image build (automatic on `main` pushes only): after tests pass, CI pushes `DOCKERHUB_USER/eos:${GITHUB_SHA}`.
3. Deployment trigger (manual): run `deploy.yml` using `workflow_dispatch` and choose `deploy_target` as `frontend`, `backend`, or `both`.
4. Conditional execution: the selected jobs run in parallel when `both` is chosen.
5. Backend path details: when target includes backend, provide `backend_commit_sha`; GitHub Actions verifies the SHA-tagged image exists, retags it as `latest`, pushes `latest`, waits briefly for Docker Hub indexing, and triggers the Render Deploy Hook.
6. Runtime rollout: Render pulls `DOCKERHUB_USER/eos:latest` and deploys the new backend version; Vercel deploys frontend when selected.

### 10.7 Manual Deployment Runbook (GitHub Actions)

Use this runbook when you want to release frontend, backend, or both from one workflow:

1. Open your GitHub repository, then go to Actions.
2. Select the `Unified Deployment` workflow (`deploy.yml`).
3. Click **Run workflow**.
4. Set `deploy_target` to one of:
  - `frontend`: deploy frontend to Vercel only.
  - `backend`: deploy backend to Render only.
  - `both`: deploy frontend and backend in parallel.
5. If target includes backend (`backend` or `both`), set `backend_commit_sha` to a full 40-character lowercase commit SHA from a successful `main` backend image build.
6. Run the workflow and monitor logs:
  - Validate branch and inputs.
  - Run frontend deployment to Vercel when selected.
  - Verify immutable backend image exists, promote SHA tag to `latest`, and trigger Render deploy hook when backend is selected.

If this workflow completes successfully, selected targets are deployed.

### 10.8 Configure Health Checks and Uptime Monitoring

1. Health Check Path: In Render service settings, configure the backend health check endpoint as `/api/v1/health` (or `/actuator/health` if you enable Spring Boot Actuator).
2. Uptime Monitoring: Optionally, set up periodic HTTP pings (for example every 5 minutes) using tools like UptimeRobot targeting your configured health endpoint (for example `/api/v1/health`).

### 10.9 Common Issues and Fixes

1. "No public image found" on Render: Ensure backend CI has pushed the SHA-tagged image and `deploy.yml` ran with `deploy_target=backend` or `both` to promote it to `latest`. Also ensure Docker Hub repository visibility/access settings are correct.
2. Render Deploy Hook fails in GitHub Actions: Verify the `RENDER_DEPLOY_HOOK` secret exactly matches the URL from Render Settings.
3. App starts but cannot connect to DB: Re-check DB environment variables in Render.
4. Container crashes immediately: Check Render logs. If there is an Out Of Memory (OOM) error, add `JAVA_OPTS` with memory limits in Render environment variables.
