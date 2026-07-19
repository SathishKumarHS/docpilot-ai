from app.services.embedding_service import embedding_service
from app.services.qdrant_service import qdrant_service


class SearchService:

    def search(self, query: str):
        embedding_response = embedding_service.generate_embedding(query)

        return qdrant_service.search(
            embedding=embedding_response.embedding,
            limit=5,
        )


search_service = SearchService()