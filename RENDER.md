# Render Deployment

## Backend Web Service

Use Docker for the backend service. Render's native runtimes do not include Java, so a Node service will fail with `JAVA_HOME is not set`.

Root directory:

```text
.
```

Runtime / Language:

```text
Docker
```

Dockerfile path:

```text
./Dockerfile
```

Environment variables:

```text
SPRING_PROFILES_ACTIVE=dev
DATABASE_URL=<Render internal database URL>
GOOGLE_CLIENT_ID=<google-client-id>.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=<google-client-secret>
JWT_SECRET=<at-least-32-random-characters>
JWT_EXPIRATION_MILLIS=86400000
CORS_ALLOWED_ORIGIN_PATTERNS=https://<frontend-service>.vercel.app,https://*.vercel.app
PORT=10000
BOOTSTRAP_ADMIN_ID=11111111-1111-1111-1111-111111111111
BOOTSTRAP_ADMIN_NAME=AFC Admin
BOOTSTRAP_ADMIN_EMAIL=<admin-email>
BOOTSTRAP_ADMIN_PASSWORD=<admin-password>
BOOTSTRAP_MEMBER_ID=22222222-2222-2222-2222-222222222222
BOOTSTRAP_MEMBER_NAME=AFC Demo Member
BOOTSTRAP_MEMBER_EMAIL=<member-email>
BOOTSTRAP_MEMBER_PASSWORD=<member-password>
```

For Google login, add the frontend URL to Google Cloud Console as an authorized JavaScript origin.

Database schema is managed by Liquibase. On first startup, the app applies:

```text
src/main/resources/db/changelog/db.changelog-master.yaml
```

Render usually provides PostgreSQL URLs as `postgresql://user:password@host/database`. The app converts that to the JDBC URL format during startup.
