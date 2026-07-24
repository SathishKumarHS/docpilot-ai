import pytest
from app.services.search_service import SearchService


class TestSearchService:

    def test_search(self, mock_gemini_service, mock_qdrant_service):
        svc = SearchService()
        with (
            pytest.MonkeyPatch.context() as mp,
        ):
            mp.setattr("app.services.search_service.gemini_service", mock_gemini_service)
            mp.setattr("app.services.search_service.qdrant_service", mock_qdrant_service)

            results = svc.search(query="test query", client_id="client-1")

        assert len(results) == 2
        assert results[0].document_id == "doc-1"
        mock_gemini_service.generate_embedding.assert_called_once_with("test query")
        mock_qdrant_service.search.assert_called_once_with(
            embedding=[0.1, 0.2, 0.3],
            client_id="client-1",
            limit=5,
        )
