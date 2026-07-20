from app.services.gemini_service import gemini_service
from app.services.qdrant_service import qdrant_service


class SearchService:

    def search(self, query: str, client_id: str):
        embedding_response = gemini_service.generate_embedding(query)

        return qdrant_service.search(
            embedding=embedding_response.embedding,
            client_id=client_id,
            limit=5,
        )


search_service = SearchService()
