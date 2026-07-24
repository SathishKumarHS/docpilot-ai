import pytest
from uuid import UUID
from unittest.mock import MagicMock, patch

from app.services.document_service import DocumentService
from app.services.qdrant_service import qdrant_service


@pytest.mark.asyncio
async def test_delete_document():
    svc = DocumentService()

    with patch.object(qdrant_service, "delete") as mock_delete:
        await svc.delete_document(
            UUID("550e8400-e29b-41d4-a716-446655440000"),
            "client-1",
        )

    mock_delete.assert_called_once_with(
        UUID("550e8400-e29b-41d4-a716-446655440000"),
        "client-1",
    )
