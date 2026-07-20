from uuid import UUID

from pydantic import BaseModel


class VectorPayload(BaseModel):
    document_id: UUID
    chunk_index: int
    text: str
    client_id: str