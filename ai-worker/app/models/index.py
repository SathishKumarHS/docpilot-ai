from pydantic import BaseModel
from uuid import UUID


class ChunkRequest(BaseModel):
    chunk_id: UUID
    chunk_index: int
    text: str


class IndexDocumentRequest(BaseModel):
    document_id: UUID
    chunks: list[ChunkRequest]


class IndexDocumentResponse(BaseModel):
    indexed_chunks: int