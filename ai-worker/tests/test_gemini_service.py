import pytest
from unittest.mock import MagicMock, patch

from app.services.gemini_service import GeminiService
from app.exceptions import EmbeddingError, AnswerGenerationError


def make_mock_embed_response(values):
    embed = MagicMock()
    embed.values = values
    resp = MagicMock()
    resp.embeddings = [embed]
    return resp


def make_mock_batch_embed_response(*values_list):
    resp = MagicMock()
    resp.embeddings = []
    for values in values_list:
        embed = MagicMock()
        embed.values = values
        resp.embeddings.append(embed)
    return resp


def make_mock_content_response(text):
    resp = MagicMock()
    resp.text = text
    return resp


class TestGeminiService:

    def test_generate_embedding_success(self):
        svc = GeminiService()
        mock_client = MagicMock()
        mock_client.models.embed_content.return_value = make_mock_embed_response([0.1, 0.2, 0.3])

        with patch("app.services.gemini_service.client", mock_client):
            result = svc.generate_embedding("hello")

        assert result.model == "gemini-embedding-001"
        assert result.dimensions == 3
        assert result.embedding == [0.1, 0.2, 0.3]
        mock_client.models.embed_content.assert_called_once_with(
            model="gemini-embedding-001",
            contents="hello",
        )

    def test_generate_embedding_client_error(self):
        svc = GeminiService()
        mock_client = MagicMock()
        from google.genai import errors as genai_errors
        mock_client.models.embed_content.side_effect = genai_errors.ClientError(
            code=400, response_json={"error": {"message": "bad request"}}
        )

        with patch("app.services.gemini_service.client", mock_client):
            with pytest.raises(EmbeddingError, match="Gemini API error"):
                svc.generate_embedding("hello")

    def test_generate_embedding_unexpected_error(self):
        svc = GeminiService()
        mock_client = MagicMock()
        mock_client.models.embed_content.side_effect = RuntimeError("connection failed")

        with patch("app.services.gemini_service.client", mock_client):
            with pytest.raises(EmbeddingError, match="connection failed"):
                svc.generate_embedding("hello")

    def test_generate_embeddings_batch_success(self):
        svc = GeminiService()
        mock_client = MagicMock()
        mock_client.models.embed_content.return_value = make_mock_batch_embed_response(
            [0.1, 0.2, 0.3], [0.4, 0.5, 0.6]
        )

        with patch("app.services.gemini_service.client", mock_client):
            results = svc.generate_embeddings_batch(["text a", "text b"])

        assert len(results) == 2
        assert results[0].embedding == [0.1, 0.2, 0.3]
        assert results[1].embedding == [0.4, 0.5, 0.6]

    def test_generate_embeddings_batch_client_error(self):
        svc = GeminiService()
        mock_client = MagicMock()
        from google.genai import errors as genai_errors
        mock_client.models.embed_content.side_effect = genai_errors.ClientError(
            code=400, response_json={"error": {"message": "rate limited"}}
        )

        with patch("app.services.gemini_service.client", mock_client):
            with pytest.raises(EmbeddingError, match="Gemini API error"):
                svc.generate_embeddings_batch(["text"])

    def test_generate_answer_success(self):
        svc = GeminiService()
        mock_client = MagicMock()
        mock_client.models.generate_content.return_value = make_mock_content_response("the answer")

        with patch("app.services.gemini_service.client", mock_client):
            result = svc.generate_answer("some prompt")

        assert result == "the answer"
        mock_client.models.generate_content.assert_called_once_with(
            model="gemini-3.5-flash",
            contents="some prompt",
        )

    def test_generate_answer_client_error(self):
        svc = GeminiService()
        mock_client = MagicMock()
        from google.genai import errors as genai_errors
        mock_client.models.generate_content.side_effect = genai_errors.ClientError(
            code=403, response_json={"error": {"message": "forbidden"}}
        )

        with patch("app.services.gemini_service.client", mock_client):
            with pytest.raises(AnswerGenerationError, match="Gemini API error"):
                svc.generate_answer("prompt")

    def test_generate_answer_unexpected_error(self):
        svc = GeminiService()
        mock_client = MagicMock()
        mock_client.models.generate_content.side_effect = RuntimeError("timeout")

        with patch("app.services.gemini_service.client", mock_client):
            with pytest.raises(AnswerGenerationError, match="timeout"):
                svc.generate_answer("prompt")
