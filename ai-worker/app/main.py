from fastapi import FastAPI

from app.api.embedding import router as embedding_router
from app.api.health import router as health_router
from app.api.search import router as search_router
from app.api.ask import router as ask_router
from app.api.index import router as index_router

from contextlib import asynccontextmanager

from app.services.qdrant_service import qdrant_service

@asynccontextmanager
async def lifespan(app: FastAPI):
    print("Starting AI Worker...")

    qdrant_service.create_collection()

    yield

    print("Stopping AI Worker...")

app = FastAPI(
    title="DocPilot AI Worker",
    lifespan=lifespan,
)

app.include_router(health_router)
app.include_router(embedding_router)
app.include_router(search_router)
app.include_router(ask_router)
app.include_router(index_router)