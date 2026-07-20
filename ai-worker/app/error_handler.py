from fastapi import Request
from fastapi.responses import JSONResponse

from app.exceptions import AiWorkerError


async def ai_worker_error_handler(request: Request, exc: AiWorkerError):
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "error": exc.detail,
            "message": str(exc),
        },
    )
