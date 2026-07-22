from uuid import UUID
from app.config.settings import settings
from app.exceptions import VectorDatabaseError
from app.models.search import SearchResult
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
        try:
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

            qdrant_client.create_payload_index(
                collection_name=settings.qdrant_collection_name,
                field_name="client_id",
                field_schema=PayloadSchemaType.KEYWORD,
            )

            print(f"Collection '{self.COLLECTION_NAME}' created.")
        except Exception as e:
            raise VectorDatabaseError(f"Failed to create collection: {e}")

    def upsert_embedding(
        self,
        point_id: str,
        embedding: list[float],
        payload: VectorPayload,
        ):
        try:
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
        except Exception as e:
            raise VectorDatabaseError(f"Failed to upsert embedding: {e}")

    def upsert_embeddings_batch(
        self,
        points: list[tuple[str, list[float], VectorPayload]],
    ):
        try:
            qdrant_client.upsert(
                collection_name=self.COLLECTION_NAME,
                points=[
                    PointStruct(
                        id=point_id,
                        vector=embedding,
                        payload=payload.model_dump(),
                    )
                    for point_id, embedding, payload in points
                ],
            )
        except Exception as e:
            raise VectorDatabaseError(f"Failed to upsert embeddings batch: {e}")

    def search(
        self,
        embedding: list[float],
        client_id: str,
        document_id: str | None = None,
        limit: int = 5,
    ):
        try:
            conditions = [
                FieldCondition(
                    key="client_id",
                    match=MatchValue(value=client_id),
                )
            ]
            if document_id is not None:
                conditions.append(
                    FieldCondition(
                        key="document_id",
                        match=MatchValue(value=document_id),
                    )
                )
            response = qdrant_client.query_points(
                collection_name=self.COLLECTION_NAME,
                query=embedding,
                limit=limit,
                query_filter=Filter(must=conditions),
            )
        except Exception as e:
            raise VectorDatabaseError(f"Failed to search embeddings: {e}")

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
        document_id: UUID,
        client_id: str,
    ):
        try:
            qdrant_client.delete(
                collection_name=self.COLLECTION_NAME,
                points_selector=FilterSelector(
                    filter=Filter(
                        must=[
                            FieldCondition(
                                key="document_id",
                                match=MatchValue(value=str(document_id)),
                            ),
                            FieldCondition(
                                key="client_id",
                                match=MatchValue(value=client_id),
                            ),
                        ]
                    )
                ),
            )
        except Exception as e:
            raise VectorDatabaseError(f"Failed to delete document: {e}")


qdrant_service = QdrantService()
