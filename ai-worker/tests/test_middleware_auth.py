import pytest
from unittest.mock import MagicMock, AsyncMock

from app.middleware.auth import ServiceKeyMiddleware, SERVICE_KEY_HEADER, PUBLIC_PATHS


@pytest.mark.asyncio
async def test_middleware_public_path_skips_auth():
    settings = MagicMock()
    settings.service_api_key = "secret"

    middleware = ServiceKeyMiddleware(MagicMock())

    for path in PUBLIC_PATHS:
        request = MagicMock()
        request.url.path = path

        call_next = AsyncMock()

        with (
            pytest.MonkeyPatch.context() as mp,
        ):
            mp.setattr("app.middleware.auth.settings", settings)
            response = await middleware.dispatch(request, call_next)

        call_next.assert_awaited_once()
        assert response == call_next.return_value


@pytest.mark.asyncio
async def test_middleware_valid_key():
    settings = MagicMock()
    settings.service_api_key = "secret"

    middleware = ServiceKeyMiddleware(MagicMock())

    request = MagicMock()
    request.url.path = "/documents/index"
    request.headers.get.return_value = "secret"

    call_next = AsyncMock()

    with (
        pytest.MonkeyPatch.context() as mp,
    ):
        mp.setattr("app.middleware.auth.settings", settings)
        response = await middleware.dispatch(request, call_next)

    request.headers.get.assert_called_once_with(SERVICE_KEY_HEADER)
    call_next.assert_awaited_once()


@pytest.mark.asyncio
async def test_middleware_missing_key():
    settings = MagicMock()
    settings.service_api_key = "secret"

    middleware = ServiceKeyMiddleware(MagicMock())

    request = MagicMock()
    request.url.path = "/documents/index"
    request.headers.get.return_value = None

    call_next = AsyncMock()

    with (
        pytest.MonkeyPatch.context() as mp,
    ):
        mp.setattr("app.middleware.auth.settings", settings)
        with pytest.raises(Exception) as exc_info:
            await middleware.dispatch(request, call_next)

    assert exc_info.value.status_code == 401
    call_next.assert_not_awaited()


@pytest.mark.asyncio
async def test_middleware_wrong_key():
    settings = MagicMock()
    settings.service_api_key = "secret"

    middleware = ServiceKeyMiddleware(MagicMock())

    request = MagicMock()
    request.url.path = "/documents/index"
    request.headers.get.return_value = "wrong"

    call_next = AsyncMock()

    with (
        pytest.MonkeyPatch.context() as mp,
    ):
        mp.setattr("app.middleware.auth.settings", settings)
        with pytest.raises(Exception) as exc_info:
            await middleware.dispatch(request, call_next)

    assert exc_info.value.status_code == 401
    call_next.assert_not_awaited()
