# DocPilot AI Worker

Python microservice for document indexing, semantic search, and RAG Q&A. Serves both a gRPC API (internal) and a FastAPI REST API (debug/health).

## Tech

- **Framework:** FastAPI + Python 3.14
- **AI:** Google Gemini API (embeddings + text generation)
- **Vector DB:** Qdrant (semantic search)
- **gRPC:** backend service integration (IndexDocument, Ask, DeleteDocument)
- **Auth:** Service key (gRPC metadata + FastAPI middleware)

## Project structure

```
ai-worker/app/
├── api/           # FastAPI REST routes (health, embedding, search, ask, index, delete)
├── clients/       # External API clients (Gemini, Qdrant)
├── config/        # Pydantic settings
├── grpc/          # gRPC servicer + generated protobuf stubs
├── middleware/     # Service key auth middleware
├── models/        # Pydantic schemas
├── prompts/       # RAG prompt templates (with conversation history support)
└── services/      # Business logic (indexing, search, ask, document, Qdrant, Gemini)
```

## Ports

| Port | Protocol | Description |
|------|----------|-------------|
| 8000 | HTTP     | FastAPI REST (health, debug endpoints) |
| 50051 | gRPC    | Internal service calls |

## Running

```bash
pip install -r requirements.txt

# Tests
PYTHONPATH=. pytest tests/

# Regenerate protobuf stubs (from repo root)
make proto-gen-python
```

## gRPC Services

| RPC | Request → Response | Description |
|-----|--------------------|-------------|
| `IndexDocument` | `IndexDocumentRequest` → `IndexDocumentResponse` | Chunk + embed + store in Qdrant |
| `Ask` | `AskRequest` → `AskResponse` | Embed query → search Qdrant → RAG prompt → Gemini answer |
| `DeleteDocument` | `DeleteDocumentRequest` → `DeleteDocumentResponse` | Remove vectors from Qdrant |
