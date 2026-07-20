from app.models.index import (
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
        client_id: str,
    ) -> IndexDocumentResponse:

        texts = [chunk.text for chunk in request.chunks]

        embeddings = gemini_service.generate_embeddings_batch(texts)

        points = []
        for chunk, emb in zip(request.chunks, embeddings):
            payload = VectorPayload(
                document_id=request.document_id,
                chunk_index=chunk.chunk_index,
                text=chunk.text,
                client_id=client_id,
            )
            points.append((str(chunk.chunk_id), emb.embedding, payload))

        qdrant_service.upsert_embeddings_batch(points)

        return IndexDocumentResponse(
            indexed_chunks=len(request.chunks)
        )


index_service = IndexService()
