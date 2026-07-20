
from app.services.document_service import document_service
from uuid import UUID
from fastapi import APIRouter

router = APIRouter(prefix="/documents", tags=["health"])

@router.delete("/{document_id}")
async def delete_document(document_id: UUID):
    await document_service.delete_document(document_id)
    return {"deleted": True}