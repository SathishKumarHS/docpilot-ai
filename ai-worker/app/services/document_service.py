from app.services.qdrant_service import qdrant_service
from uuid import UUID


class DocumentService:

    async def delete_document(self, document_id: UUID, client_id: str) -> None:
        qdrant_service.delete(document_id, client_id)


document_service = DocumentService()
