from pydantic import BaseModel, Field


class AskRequest(BaseModel):
    question: str = Field(
        ...,
        min_length=1,
        description="Question to ask about the indexed documents",
    )
    document_id: str | None = Field(
        default=None,
        description="Optional document ID to scope the question to a specific document",
    )


class AskResponse(BaseModel):
    answer: str