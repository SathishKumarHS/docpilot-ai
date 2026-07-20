from app.config.settings import settings
from qdrant_client import QdrantClient

qdrant_client = QdrantClient(
    host=settings.qdrant_url,
    api_key=settings.qdrant_api_key,
)