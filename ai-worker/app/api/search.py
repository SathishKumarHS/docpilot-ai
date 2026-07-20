from fastapi import APIRouter, Header

from app.models.search import SearchRequest
from app.services.search_service import search_service

router = APIRouter(
    prefix="/search",
    tags=["Search"],
)


@router.post("")
def search(
    request: SearchRequest,
    x_client_id: str = Header(...),
):
    results = search_service.search(request.query, x_client_id)

    return results
