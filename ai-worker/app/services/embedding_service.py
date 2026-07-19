from app.clients.gemini_client import client
from app.models.embedding import EmbeddingResponse


class EmbeddingService:

    def generate_embedding(self, text: str) -> list[float]:
        response = client.models.embed_content(
            model="gemini-embedding-001",
            contents=text,
        )

        embedding = response.embeddings[0].values

        return EmbeddingResponse(
            model="gemini-embedding-001",
            dimensions=len(embedding),
            embedding=embedding,
        )
    
    def generate_answer(self, prompt: str) -> str:
        response = client.models.generate_content(
            model="gemini-3.5-flash",
            contents=prompt,
        )

        return response.text


embedding_service = EmbeddingService()