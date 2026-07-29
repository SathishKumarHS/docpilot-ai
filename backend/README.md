# DocPilot Backend

Spring Boot 4.1 REST API (Kotlin 2.3, Java 21). Handles auth, document management, RAG Q&A, and chat history persistence.

## Tech

- **Framework:** Spring Boot 4.1 + Kotlin 2.3
- **Database:** PostgreSQL 16 (JPA/Hibernate)
- **Cache/Rate-limit:** Redis 7 (Bucket4j token bucket)
- **Storage:** MinIO (S3-compatible PDF storage)
- **gRPC:** ai-worker (Ask, IndexDocument) + feature-flag (GetFlags)
- **AI:** Gemini API via ai-worker
- **PDF:** Apache PDFBox 3.0
- **Auth:** JWT (jjwt), OAuth2 (Google), anonymous sessions
- **Docs:** Swagger UI at `/swagger-ui.html`

## Project structure

```
backend/src/main/kotlin/.../backend/
├── aiworker/       # gRPC client for ai-worker
├── ask/            # Chat Q&A, chat history (entity/repo/service/controller)
├── auth/           # Registration, login, JWT, anonymous sessions
├── config/         # Rate limiting, web config, ai-worker properties
├── core/           # Shared JPA entities and repositories
├── document/       # PDF upload, chunking, MinIO storage, cleanup scheduler
├── exception/      # Global error handler
├── featureflag/    # gRPC client for feature-flag service
├── health/         # Health check endpoint
├── oauth/          # Google OAuth2 exchange handler
└── security/       # JWT + anonymous session request filters
```

## Running

```bash
# Run tests
./gradlew test

# Run e2e tests
SERVICE_API_KEY=xxx JWT_SECRET=xxx ./gradlew e2eTest

# Regenerate protobuf stubs
./gradlew generateProto

# Build JAR
./gradlew bootJar
```

## Ports

| Port | Service |
|------|---------|
| 8080 | REST API |
| 8080 | Swagger UI |

## Environment

See `.env.example` for all variables. Required: `JWT_SECRET`, `GEMINI_API_KEY`, `SERVICE_API_KEY`.

## Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/v1/auth/register` | Public | Register |
| `POST` | `/api/v1/auth/login` | Public | Login |
| `POST` | `/api/v1/auth/anonymous-session` | Public | Anonymous session |
| `POST` | `/api/v1/auth/exchange` | Public | OAuth2 code exchange |
| `POST` | `/api/v1/documents` | Bearer/Anon | Upload PDF |
| `GET` | `/api/v1/documents` | Bearer/Anon | List documents |
| `DELETE` | `/api/v1/documents/{id}` | Bearer/Anon | Delete document |
| `POST` | `/api/v1/ask` | Bearer/Anon | Ask question (RAG with history) |
| `GET` | `/api/v1/ask/history` | Bearer | Recent chat messages |
| `GET` | `/api/v1/health` | Public | Health check |
