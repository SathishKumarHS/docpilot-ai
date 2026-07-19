from fastapi import APIRouter

from app.models.search import SearchRequest
from app.services.search_service import search_service

router = APIRouter(
    prefix="/search",
    tags=["Search"],
)


@router.post("")
def search(request: SearchRequest):
    results = search_service.search(request.query)

    return results