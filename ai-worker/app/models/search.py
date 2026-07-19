from pydantic import BaseModel, Field


class SearchRequest(BaseModel):
    query: str = Field(
        ...,
        min_length=1,
        description="Search query",
    )


class SearchResult(BaseModel):
    score: float
    document_id: str
    chunk_index: int
    text: str