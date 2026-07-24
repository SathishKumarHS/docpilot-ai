from app.exceptions import (
    AiWorkerError,
    AiServiceError,
    EmbeddingError,
    AnswerGenerationError,
    VectorDatabaseError,
    DocumentNotFoundError,
    MissingClientIdError,
)


def test_ai_worker_error_defaults():
    err = AiWorkerError()
    assert err.status_code == 500
    assert err.detail == "Internal server error"


def test_ai_service_error():
    err = AiServiceError()
    assert err.status_code == 502
    assert err.detail == "AI service error"


def test_embedding_error():
    err = EmbeddingError()
    assert err.status_code == 502
    assert err.detail == "Failed to generate embedding"
    assert isinstance(err, AiServiceError)


def test_answer_generation_error():
    err = AnswerGenerationError("Gemini API error: rate limit")
    assert err.status_code == 502
    assert "Gemini API error" in str(err)
    assert isinstance(err, AiServiceError)


def test_vector_database_error():
    err = VectorDatabaseError("connection refused")
    assert err.status_code == 503
    assert "connection refused" in str(err)


def test_document_not_found_error():
    err = DocumentNotFoundError()
    assert err.status_code == 404
    assert err.detail == "Document not found"


def test_missing_client_id_error():
    err = MissingClientIdError()
    assert err.status_code == 400
    assert err.detail == "Missing X-Client-Id header"
