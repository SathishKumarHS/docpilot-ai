from fastapi import APIRouter

from app.models.index import (
    IndexDocumentRequest,
    IndexDocumentResponse,
)
from app.services.index_service import index_service

router = APIRouter(
    prefix="/documents",
    tags=["Documents"],
)


@router.post(
    "/index",
    response_model=IndexDocumentResponse,
)
def index_document(request: IndexDocumentRequest):
    return index_service.index_document(request)