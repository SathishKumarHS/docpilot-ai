import os

os.environ.setdefault("GEMINI_API_KEY", "test-key")
os.environ.setdefault("QDRANT_URL", "http://localhost:6333")
os.environ.setdefault("QDRANT_API_KEY", "test-key")
os.environ.setdefault("QDRANT_COLLECTION_NAME", "test-collection")
os.environ.setdefault("SERVICE_API_KEY", "test-key")

import pytest
from uuid import UUID
from unittest.mock import MagicMock, patch

from app.models.index import ChunkRequest, IndexDocumentRequest
from app.models.search import SearchResult
from app.models.embedding import EmbeddingResponse


@pytest.fixture
def mock_embedding():
    return EmbeddingResponse(
        model="gemini-embedding-001",
        dimensions=3,
        embedding=[0.1, 0.2, 0.3],
    )


@pytest.fixture
def mock_embeddings():
    return [
        EmbeddingResponse(model="gemini-embedding-001", dimensions=3, embedding=[0.1, 0.2, 0.3]),
        EmbeddingResponse(model="gemini-embedding-001", dimensions=3, embedding=[0.4, 0.5, 0.6]),
    ]


@pytest.fixture
def mock_search_results():
    return [
        SearchResult(score=0.95, document_id="doc-1", chunk_index=0, text="test context"),
        SearchResult(score=0.80, document_id="doc-1", chunk_index=1, text="more context"),
    ]


@pytest.fixture
def sample_index_request():
    return IndexDocumentRequest(
        document_id=UUID("550e8400-e29b-41d4-a716-446655440000"),
        chunks=[
            ChunkRequest(chunk_id=UUID("660e8400-e29b-41d4-a716-446655440001"), chunk_index=0, text="chunk one"),
            ChunkRequest(chunk_id=UUID("660e8400-e29b-41d4-a716-446655440002"), chunk_index=1, text="chunk two"),
        ],
    )


@pytest.fixture
def mock_gemini_service(mock_embedding, mock_embeddings):
    svc = MagicMock()
    svc.generate_embedding.return_value = mock_embedding
    svc.generate_embeddings_batch.return_value = mock_embeddings
    svc.generate_answer.return_value = "test answer"
    return svc


@pytest.fixture
def mock_qdrant_service(mock_search_results):
    svc = MagicMock()
    svc.search.return_value = mock_search_results
    return svc
