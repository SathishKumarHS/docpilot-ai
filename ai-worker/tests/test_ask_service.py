import pytest
from app.services.ask_service import AskService


class TestAskService:

    def test_ask_without_document_id(self, mock_gemini_service, mock_qdrant_service):
        svc = AskService()
        with (
            pytest.MonkeyPatch.context() as mp,
        ):
            mp.setattr("app.services.ask_service.gemini_service", mock_gemini_service)
            mp.setattr("app.services.ask_service.qdrant_service", mock_qdrant_service)

            result = svc.ask(question="test question", client_id="client-1")

        assert result == "test answer"
        mock_gemini_service.generate_embedding.assert_called_once_with("test question")
        mock_qdrant_service.search.assert_called_once_with(
            [0.1, 0.2, 0.3],
            client_id="client-1",
            document_id=None,
            limit=20,
        )
        mock_gemini_service.generate_answer.assert_called_once()

    def test_ask_with_document_id(self, mock_gemini_service, mock_qdrant_service):
        svc = AskService()
        with (
            pytest.MonkeyPatch.context() as mp,
        ):
            mp.setattr("app.services.ask_service.gemini_service", mock_gemini_service)
            mp.setattr("app.services.ask_service.qdrant_service", mock_qdrant_service)

            result = svc.ask(question="test question", client_id="client-1", document_id="doc-1")

        assert result == "test answer"
        mock_qdrant_service.search.assert_called_once_with(
            [0.1, 0.2, 0.3],
            client_id="client-1",
            document_id="doc-1",
            limit=20,
        )
