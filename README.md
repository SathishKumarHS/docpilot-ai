# docpilot-ai

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?logo=springboot)](backend/build.gradle.kts)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3-7F52FF?logo=kotlin)](backend/build.gradle.kts)
[![React](https://img.shields.io/badge/React-19-61DAFB?logo=react)](web/package.json)
[![TypeScript](https://img.shields.io/badge/TypeScript-6.0-3178C6?logo=typescript)](web/package.json)
[![Python](https://img.shields.io/badge/Python-3.14-3776AB?logo=python)](ai-worker/requirements.txt)
[![Go](https://img.shields.io/badge/Go-1.25-00ADD8?logo=go)](feature-flag/go.mod)
[![gRPC](https://img.shields.io/badge/gRPC-inter--service-8B5CFE)](shared/proto/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)](docker-compose.yml)

AI-powered document intelligence platform. Upload PDFs, ask questions, get answers — with authentication, rate limiting, tier-based feature flags, and a microservices architecture.

---

## Architecture

```
Web (React SPA) ──── REST ────> Backend (Kotlin/Spring Boot)
                                    │
                          ┌─────────┼────────────┬───────────┬─────────┐
                          │ gRPC    │ gRPC        │ S3        │ JPA     │ Redis
                          ▼         ▼             ▼           ▼         ▼
                    ai-worker   feature-flag    MinIO     PostgreSQL  Redis
                    (Python)    (Go)            (PDFs)    (metadata)  (rate
                          │                                           limit/
                          │ HTTP                                       cache)
                          ▼
                       Qdrant
                    (Vector DB)
```

**Data Flow**: Upload PDF → MinIO storage → PDFBox text extraction → chunking (500 chars) → gRPC `IndexDocument` → Gemini embeddings → Qdrant storage. Ask question → check rate limit + daily cap → gRPC `Ask` → embed query → Qdrant semantic search → RAG prompt → Gemini answer.

---

## Features

| Category | Details |
|---|---|
| **Auth** | Email/password, Google OAuth2, anonymous sessions, JWT access + refresh tokens |
| **Documents** | PDF upload, text extraction, chunking, MinIO storage, scheduled expiry cleanup |
| **AI Chat** | Global + per-document RAG Q&A, semantic search via Qdrant, Gemini-generated answers |
| **Feature Flags** | Tier-based limits (anonymous vs. registered), YAML-driven, cached in Redis |
| **Rate Limiting** | Token-bucket per endpoint, Redis-backed via Bucket4j |
| **Multi-Tenancy** | All data isolated by `client_id` |

---

## Tech Stack

| Service | Stack |
|---|---|
| **Frontend** | React 19, TypeScript 6, Vite 8, Tailwind CSS 3 |
| **Backend** | Kotlin 2.3, Spring Boot 4.1, JPA, PostgreSQL 16, Redis 7 |
| **AI Service** | Python 3.14, FastAPI, Gemini API, Qdrant vector DB |
| **Feature Flags** | Go 1.25, gRPC, Viper |
| **Infrastructure** | Docker Compose (7 containers), MinIO (S3 storage) |
| **Inter-service** | gRPC with protobuf, S3-compatible REST for MinIO |

---

## Quick Start

```bash
cp .env.example .env          # fill in API keys
make docker-up                # build & start all services
# open http://localhost:8080
```

### Makefile Commands

| Command | Description |
|---|---|
| `make docker-up` | Build and start all containers |
| `make docker-down` | Stop all containers |
| `make test` | Run all unit tests |
| `make proto-gen` | Regenerate gRPC stubs |

---

## Environment Variables

Key variables (see `.env.example` for full list):

| Variable | Default | Required |
|---|---|---|
| `JWT_SECRET` | — | Yes (256-bit base64) |
| `GEMINI_API_KEY` | — | Yes |
| `SERVICE_API_KEY` | — | Yes |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/docpilot` | No |
| `GOOGLE_CLIENT_ID` | — | For OAuth2 |
| `GOOGLE_CLIENT_SECRET` | — | For OAuth2 |

---

## Project Structure

```
docpilot-ai/
├── backend/                          # Spring Boot REST API (Kotlin)
│   └── src/main/kotlin/.../backend/
│       ├── auth/                     # Auth controllers, JWT, session services
│       ├── document/                 # Document upload, chunking, cleanup
│       ├── ask/                      # RAG question-answering
│       ├── aiworker/                 # gRPC client for ai-worker
│       ├── featureflag/              # gRPC client + flag cache
│       ├── security/                 # JWT + anonymous session filters
│       ├── oauth/                    # OAuth2 exchange handler
│       ├── config/                   # Rate limiting, web config
│       ├── core/                     # Entities, repositories
│       ├── exception/                # Global error handler
│       └── health/                   # Health check endpoint
├── ai-worker/                        # AI service (Python/FastAPI)
│   ├── app/
│   │   ├── api/                      # REST endpoints
│   │   ├── grpc/                     # gRPC servicer + generated stubs
│   │   ├── services/                 # Qdrant, Gemini, indexing, search
│   │   ├── clients/                  # External API clients
│   │   ├── middleware/               # Auth middleware
│   │   ├── models/                   # Pydantic schemas
│   │   ├── prompts/                  # RAG prompt templates
│   │   └── config/                   # App configuration
│   └── tests/                        # pytest test suite
├── feature-flag/                     # Feature flag service (Go)
│   ├── handler/                      # HTTP handlers
│   ├── grpc/                         # gRPC server + generated stubs
│   ├── middleware/                   # Auth middleware
│   ├── config/                       # Config loader
│   ├── data/                         # flags.yml (tier limits)
│   └── test/                         # Go test suite
├── web/                              # React SPA (TypeScript)
│   └── src/
│       ├── components/               # Shared UI components
│       ├── pages/                    # Route pages
│       ├── lib/                      # Auth utils, API client
│       └── assets/                   # Static assets
├── shared/proto/                     # Protobuf definitions (single source of truth)
│   ├── aiworker.proto                # IndexDocument, Ask, DeleteDocument
│   └── featureflag.proto             # GetFlags
├── docker-compose.yml                # 7-container orchestration
├── .env.example                      # Environment variable template
├── .github/workflows/                # CI/CD pipelines
└── Makefile                          # Build, test, run targets
```

---

## API Overview

### Backend REST (port 8080)

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Public | Register |
| `POST` | `/api/v1/auth/login` | Public | Login |
| `POST` | `/api/v1/auth/anonymous-session` | Public | Anonymous session |
| `POST` | `/api/v1/auth/exchange` | Public | OAuth2 code exchange |
| `POST` | `/api/v1/documents` | Bearer/Anon | Upload PDF |
| `GET` | `/api/v1/documents` | Bearer/Anon | List documents |
| `DELETE` | `/api/v1/documents/{id}` | Bearer/Anon | Delete document |
| `POST` | `/api/v1/ask` | Bearer/Anon | Ask question (RAG) |
| `GET` | `/api/v1/health` | Public | Health check |

### gRPC Services

**ai-worker** (`IndexDocument`, `Ask`, `DeleteDocument`) — **feature-flag** (`GetFlags`)

---

## Testing

```bash
make test                    # All unit tests
make test-backend            # JUnit 5 + Mockito
make test-ai-worker          # pytest + pytest-asyncio
make test-feature-flag       # Go testing package
```

---

## CI/CD

GitHub Actions: `ci.yml` (5 parallel jobs on push/PR), `docker.yml` (build & push to GHCR on tags).

---

## License

[MIT](LICENSE) © 2026 Sathish Kumar H S
