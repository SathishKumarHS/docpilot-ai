from app.models.index import (
    ChunkRequest,
    IndexDocumentRequest,
    IndexDocumentResponse,
)
from app.models.vector import VectorPayload
from app.services.gemini_service import gemini_service
from app.services.qdrant_service import qdrant_service


class IndexService:

    def index_document(
        self,
        request: IndexDocumentRequest,
    ) -> IndexDocumentResponse:

        for chunk in request.chunks:

            embedding_response = gemini_service.generate_embedding(
                chunk.text
            )

            payload = VectorPayload(
                document_id=request.document_id,
                chunk_index=chunk.chunk_index,
                text=chunk.text,
            )

            qdrant_service.upsert_embedding(
                point_id=str(chunk.chunk_id),
                embedding=embedding_response.embedding,
                payload=payload,
            )

        return IndexDocumentResponse(
            indexed_chunks=len(request.chunks)
        )


index_service = IndexService()