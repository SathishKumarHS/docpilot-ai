import pytest
from app.services.index_service import IndexService


class TestIndexService:

    def test_index_document(self, mock_gemini_service, mock_qdrant_service, sample_index_request):
        svc = IndexService()
        with (
            pytest.MonkeyPatch.context() as mp,
        ):
            mp.setattr("app.services.index_service.gemini_service", mock_gemini_service)
            mp.setattr("app.services.index_service.qdrant_service", mock_qdrant_service)

            result = svc.index_document(sample_index_request, "client-1")

        assert result.indexed_chunks == 2
        mock_gemini_service.generate_embeddings_batch.assert_called_once_with(
            ["chunk one", "chunk two"]
        )
        mock_qdrant_service.upsert_embeddings_batch.assert_called_once()
        args, _ = mock_qdrant_service.upsert_embeddings_batch.call_args
        points = args[0]
        assert len(points) == 2
        assert points[0][2].document_id == sample_index_request.document_id
        assert points[0][2].client_id == "client-1"
        assert points[0][2].chunk_index == 0
        assert points[0][2].text == "chunk one"
