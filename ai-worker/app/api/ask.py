from fastapi import APIRouter, Header

from app.models.ask import AskRequest, AskResponse
from app.services.ask_service import ask_service

router = APIRouter(
    prefix="/ask",
    tags=["RAG"],
)


@router.post("", response_model=AskResponse)
def ask(
    request: AskRequest,
    x_client_id: str = Header(...),
):
    answer = ask_service.ask(
        question=request.question,
        client_id=x_client_id,
        document_id=request.document_id,
    )

    return AskResponse(answer=answer)
