from pydantic import BaseModel, Field


class AskRequest(BaseModel):
    question: str = Field(
        ...,
        min_length=1,
        description="Question to ask about the indexed documents",
    )


class AskResponse(BaseModel):
    answer: str