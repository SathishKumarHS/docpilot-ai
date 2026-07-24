from fastapi import Request, HTTPException
from starlette.middleware.base import BaseHTTPMiddleware

from app.config.settings import settings

SERVICE_KEY_HEADER = "X-Service-Key"
PUBLIC_PATHS = {"/health", "/docs", "/openapi.json"}


class ServiceKeyMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next):
        if request.url.path in PUBLIC_PATHS:
            return await call_next(request)

        key = request.headers.get(SERVICE_KEY_HEADER)
        if not key or key != settings.service_api_key:
            raise HTTPException(status_code=401, detail="Invalid or missing service key")

        return await call_next(request)
