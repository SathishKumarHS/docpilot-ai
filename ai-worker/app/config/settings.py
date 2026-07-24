from pydantic_settings import BaseSettings, SettingsConfigDict
from pydantic import Field


class Settings(BaseSettings):
    gemini_api_key: str = Field(alias="GEMINI_API_KEY")
    qdrant_url: str = Field(alias="QDRANT_URL")
    qdrant_api_key: str = Field(alias="QDRANT_API_KEY")
    qdrant_collection_name: str = Field(alias="QDRANT_COLLECTION_NAME")
    service_api_key: str = Field(alias="SERVICE_API_KEY")

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
    )

settings = Settings()