from app.services.document_service import document_service
from uuid import UUID
from fastapi import APIRouter, Header

router = APIRouter(prefix="/documents", tags=["health"])


@router.delete("/{document_id}")
async def delete_document(
    document_id: UUID,
    x_client_id: str = Header(...),
):
    await document_service.delete_document(document_id, x_client_id)
    return {"deleted": True}
