from fastapi import APIRouter

router = APIRouter(prefix="/health", tags=["Embeddings"])


@router.get("")
def health():
    return {
        "status": "UP",
        "service": "ai-worker"
    }