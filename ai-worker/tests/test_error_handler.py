import pytest
from unittest.mock import Mock

from app.error_handler import ai_worker_error_handler
from app.exceptions import AiWorkerError, EmbeddingError


@pytest.mark.asyncio
async def test_ai_worker_error_handler():
    request = Mock()
    exc = EmbeddingError("test error")

    response = await ai_worker_error_handler(request, exc)

    assert response.status_code == 502
    body = response.body.decode()
    assert "Failed to generate embedding" in body
    assert "test error" in body
