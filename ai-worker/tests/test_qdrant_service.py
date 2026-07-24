import pytest
from uuid import UUID
from unittest.mock import MagicMock, patch

from app.services.qdrant_service import QdrantService
from app.models.vector import VectorPayload


class TestQdrantService:

    def test_search_success(self):
        svc = QdrantService()
        mock_client = MagicMock()

        mock_point = MagicMock()
        mock_point.score = 0.95
        mock_point.payload = {"document_id": "doc-1", "chunk_index": 0, "text": "content"}
        mock_client.query_points.return_value.points = [mock_point]

        with patch("app.services.qdrant_service.qdrant_client", mock_client):
            results = svc.search(
                embedding=[0.1, 0.2, 0.3],
                client_id="client-1",
                document_id=None,
                limit=5,
            )

        assert len(results) == 1
        assert results[0].document_id == "doc-1"
        assert results[0].score == 0.95
        assert results[0].text == "content"

    def test_search_with_document_id(self):
        svc = QdrantService()
        mock_client = MagicMock()
        mock_client.query_points.return_value.points = []

        with patch("app.services.qdrant_service.qdrant_client", mock_client):
            svc.search(
                embedding=[0.1, 0.2, 0.3],
                client_id="client-1",
                document_id="doc-1",
            )

        call_args = mock_client.query_points.call_args
        qfilter = call_args.kwargs["query_filter"]
        conditions = qfilter.must
        assert len(conditions) == 2
        assert conditions[0].key == "client_id"
        assert conditions[1].key == "document_id"

    def test_search_error(self):
        svc = QdrantService()
        mock_client = MagicMock()
        mock_client.query_points.side_effect = Exception("qdrant down")

        with (
            patch("app.services.qdrant_service.qdrant_client", mock_client),
            pytest.raises(Exception, match="Failed to search embeddings"),
        ):
            svc.search(embedding=[0.1], client_id="c1")

    def test_upsert_embeddings_batch(self):
        svc = QdrantService()
        mock_client = MagicMock()

        points = [
            ("id-1", [0.1, 0.2], VectorPayload(
                document_id=UUID("550e8400-e29b-41d4-a716-446655440000"),
                chunk_index=0, text="hello", client_id="c1",
            )),
        ]

        with patch("app.services.qdrant_service.qdrant_client", mock_client):
            svc.upsert_embeddings_batch(points)

        mock_client.upsert.assert_called_once()
        call_args = mock_client.upsert.call_args
        assert len(call_args.kwargs["points"]) == 1

    def test_delete_document(self):
        svc = QdrantService()
        mock_client = MagicMock()

        with patch("app.services.qdrant_service.qdrant_client", mock_client):
            svc.delete(
                document_id=UUID("550e8400-e29b-41d4-a716-446655440000"),
                client_id="client-1",
            )

        mock_client.delete.assert_called_once()
        call_args = mock_client.delete.call_args
        selector = call_args.kwargs["points_selector"]
        conditions = selector.filter.must
        assert len(conditions) == 2
        assert conditions[0].key == "document_id"
        assert conditions[1].key == "client_id"
