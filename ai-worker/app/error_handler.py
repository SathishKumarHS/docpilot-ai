from fastapi import Request
from fastapi.responses import JSONResponse

from app.core.logging import get_logger
from app.exceptions import AiWorkerError

log = get_logger(__name__)


async def ai_worker_error_handler(request: Request, exc: AiWorkerError):
    log.error(
        "request %s %s failed status=%d error=%s",
        request.method,
        request.url.path,
        exc.status_code,
        exc,
    )
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "error": exc.detail,
            "message": str(exc),
        },
    )
