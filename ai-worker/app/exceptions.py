class AiWorkerError(Exception):
    status_code: int = 500
    detail: str = "Internal server error"


class AiServiceError(AiWorkerError):
    status_code = 502
    detail = "AI service error"


class EmbeddingError(AiServiceError):
    detail = "Failed to generate embedding"


class AnswerGenerationError(AiServiceError):
    detail = "Failed to generate answer"


class VectorDatabaseError(AiWorkerError):
    status_code = 503
    detail = "Vector database error"


class DocumentNotFoundError(AiWorkerError):
    status_code = 404
    detail = "Document not found"


class MissingClientIdError(AiWorkerError):
    status_code = 400
    detail = "Missing X-Client-Id header"
