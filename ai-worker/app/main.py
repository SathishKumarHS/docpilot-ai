import asyncio
from os import environ

from fastapi import FastAPI

from app.api.embedding import router as embedding_router
from app.api.health import router as health_router
from app.api.search import router as search_router
from app.api.ask import router as ask_router
from app.api.index import router as index_router
from app.api.delete import router as document_router

from contextlib import asynccontextmanager

from app.exceptions import AiWorkerError
from app.error_handler import ai_worker_error_handler
from app.grpc.service import serve_grpc
from app.middleware.auth import ServiceKeyMiddleware
from app.services.qdrant_service import qdrant_service

GRPC_PORT = int(environ.get("GRPC_PORT", "50051"))

@asynccontextmanager
async def lifespan(app: FastAPI):
    print("Starting AI Worker...")

    qdrant_service.create_collection()

    grpc_task = asyncio.create_task(serve_grpc(GRPC_PORT))

    yield

    grpc_task.cancel()
    print("Stopping AI Worker...")

app = FastAPI(
    title="DocPilot AI Worker",
    lifespan=lifespan,
)

app.add_exception_handler(AiWorkerError, ai_worker_error_handler)
app.add_middleware(ServiceKeyMiddleware)

app.include_router(health_router)
app.include_router(embedding_router)
app.include_router(search_router)
app.include_router(ask_router)
app.include_router(index_router)
app.include_router(document_router)