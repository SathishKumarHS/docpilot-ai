from fastapi import APIRouter

from app.models.ask import AskRequest, AskResponse
from app.services.ask_service import ask_service

router = APIRouter(
    prefix="/ask",
    tags=["RAG"],
)


@router.post("", response_model=AskResponse)
def ask(request: AskRequest):
    answer = ask_service.ask(request.question)

    return AskResponse(answer=answer)