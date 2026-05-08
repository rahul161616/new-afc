# Render Deployment

## Backend Web Service

Root directory:

```text
.
```

Build command:

```bash
./gradlew build
```

Start command:

```bash
java -jar build/libs/afc-0.0.1-SNAPSHOT.jar
```

Environment variables:

```text
SPRING_PROFILES_ACTIVE=dev
DATABASE_URL=jdbc:postgresql://<host>:<port>/<database>
DATABASE_USERNAME=<database-user>
DATABASE_PASSWORD=<database-password>
GOOGLE_CLIENT_ID=<google-client-id>.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=<google-client-secret>
JWT_SECRET=<at-least-32-random-characters>
JWT_EXPIRATION_MILLIS=86400000
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
