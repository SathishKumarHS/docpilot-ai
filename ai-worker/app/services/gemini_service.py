from collections.abc import Generator

from google.genai import errors as genai_errors
from google.genai.types import GenerateContentResponse

from app.core.logging import get_logger
from app.clients.gemini_client import client
from app.exceptions import EmbeddingError, AnswerGenerationError
from app.models.embedding import EmbeddingResponse

log = get_logger(__name__)


class GeminiService:

    def generate_embedding(self, text: str) -> EmbeddingResponse:
        try:
            response = client.models.embed_content(
                model="gemini-embedding-001",
                contents=text,
            )
        except genai_errors.ClientError as e:
            log.error("Gemini embedding failed: %s", e.message)
            raise EmbeddingError(f"Gemini API error: {e.message}")
        except Exception as e:
            log.exception("Gemini embedding failed")
            raise EmbeddingError(str(e))

        embedding = response.embeddings[0].values

        return EmbeddingResponse(
            model="gemini-embedding-001",
            dimensions=len(embedding),
            embedding=embedding,
        )

    def generate_embeddings_batch(self, texts: list[str]) -> list[EmbeddingResponse]:
        try:
            response = client.models.embed_content(
                model="gemini-embedding-001",
                contents=texts,
            )
        except genai_errors.ClientError as e:
            log.error("Gemini batch embedding failed: %s", e.message)
            raise EmbeddingError(f"Gemini API error: {e.message}")
        except Exception as e:
            log.exception("Gemini batch embedding failed")
            raise EmbeddingError(str(e))

        results = []
        for e in response.embeddings:
            results.append(EmbeddingResponse(
                model="gemini-embedding-001",
                dimensions=len(e.values),
                embedding=e.values,
            ))
        return results

    def generate_answer(self, prompt: str) -> str:
        try:
            response = client.models.generate_content(
                model="gemini-3.5-flash",
                contents=prompt,
            )
        except genai_errors.ClientError as e:
            log.error("Gemini answer generation failed: %s", e.message)
            raise AnswerGenerationError(f"Gemini API error: {e.message}")
        except Exception as e:
            log.exception("Gemini answer generation failed")
            raise AnswerGenerationError(str(e))

        return response.text

    def generate_answer_stream(self, prompt: str) -> Generator[str, None, None]:
        try:
            stream: GenerateContentResponse = client.models.generate_content_stream(
                model="gemini-3.5-flash",
                contents=prompt,
            )
            for chunk in stream:
                if chunk.text:
                    yield chunk.text
        except genai_errors.ClientError as e:
            log.error("Gemini streaming generation failed: %s", e.message)
            raise AnswerGenerationError(f"Gemini API error: {e.message}")
        except Exception as e:
            log.exception("Gemini streaming generation failed")
            raise AnswerGenerationError(str(e))


gemini_service = GeminiService()
