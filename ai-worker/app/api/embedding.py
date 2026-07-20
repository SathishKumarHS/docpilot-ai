from fastapi import APIRouter, Header

from app.models.embedding import EmbeddingRequest, EmbeddingResponse
from app.services.gemini_service import gemini_service
from uuid import uuid4

from app.models.vector import VectorPayload
from app.services.qdrant_service import qdrant_service

router = APIRouter(prefix="/embeddings", tags=["Embeddings"])


@router.post("", response_model=EmbeddingResponse)
def generate_embedding(
    request: EmbeddingRequest,
    x_client_id: str = Header(...),
):
    embedding_response = gemini_service.generate_embedding(request.text)

    payload = VectorPayload(
        document_id=uuid4(),
        chunk_index=0,
        text=request.text,
        client_id=x_client_id,
    )

    qdrant_service.upsert_embedding(
        embedding=embedding_response.embedding,
        payload=payload,
    )

    return embedding_response
