from google.genai import errors as genai_errors

from app.clients.gemini_client import client
from app.exceptions import EmbeddingError, AnswerGenerationError
from app.models.embedding import EmbeddingResponse


class GeminiService:

    def generate_embedding(self, text: str) -> EmbeddingResponse:
        try:
            response = client.models.embed_content(
                model="gemini-embedding-001",
                contents=text,
            )
        except genai_errors.ClientError as e:
            raise EmbeddingError(f"Gemini API error: {e.message}")
        except Exception as e:
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
            raise EmbeddingError(f"Gemini API error: {e.message}")
        except Exception as e:
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
            raise AnswerGenerationError(f"Gemini API error: {e.message}")
        except Exception as e:
            raise AnswerGenerationError(str(e))

        return response.text


gemini_service = GeminiService()
