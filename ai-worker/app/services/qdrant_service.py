from uuid import UUID
from app.config.settings import settings
from app.models.search import SearchResult
from app.api import embedding
from qdrant_client.models import Distance, VectorParams
from qdrant_client.http.models import PayloadSchemaType

from app.clients.qdrant_client import qdrant_client
from uuid import uuid4
from qdrant_client.models import PointStruct
from app.models.vector import VectorPayload

from qdrant_client.http.models import (
    Filter,
    FilterSelector,
    FieldCondition,
    MatchValue,
)

class QdrantService:

    COLLECTION_NAME = settings.qdrant_collection_name

    def create_collection(self):
        collections = qdrant_client.get_collections()

        existing = [
            collection.name
            for collection in collections.collections
        ]

        if self.COLLECTION_NAME in existing:
            print(f"Collection '{self.COLLECTION_NAME}' already exists.")
            return

        qdrant_client.create_collection(
            collection_name=self.COLLECTION_NAME,
            vectors_config=VectorParams(
                size=3072,
                distance=Distance.COSINE,
            ),
        )

        qdrant_client.create_payload_index(
            collection_name=settings.qdrant_collection_name,
            field_name="document_id",
            field_schema=PayloadSchemaType.KEYWORD,
        )

        print(f"Collection '{self.COLLECTION_NAME}' created.")

    def upsert_embedding(
        self,
        point_id: str,
        embedding: list[float],
        payload: VectorPayload,
        ):
        qdrant_client.upsert(
            collection_name=self.COLLECTION_NAME,
            points=[
                PointStruct(
                    id=point_id,
                    vector=embedding,
                    payload=payload.model_dump(),
                )
            ],
        )
    
    def search(
        self,
        embedding: list[float],
        limit: int = 5,
    ):
        response = qdrant_client.query_points(
            collection_name=self.COLLECTION_NAME,
            query=embedding,
            limit=limit,
        )

        results = []

        for point in response.points:
            results.append(
                SearchResult(
                score=point.score,
                document_id=point.payload["document_id"],
                chunk_index=point.payload["chunk_index"],
                text=point.payload["text"],
            )
        )

        return results

    def delete(
        self,
        document_id: UUID
    ):
        qdrant_client.delete(
            collection_name=self.COLLECTION_NAME,
            points_selector=FilterSelector(
                filter=Filter(
                    must=[
                        FieldCondition(
                            key="document_id",
                            match=MatchValue(value=str(document_id)),
                        )
                    ]
                )
            ),
        )


qdrant_service = QdrantService()