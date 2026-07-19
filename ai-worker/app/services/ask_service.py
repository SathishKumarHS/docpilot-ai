from app.prompts.rag_prompt import build_rag_prompt
from app.services.gemini_service import gemini_service
from app.services.qdrant_service import qdrant_service


class AskService:

    def ask(self, question: str) -> str:
        embedding_response = gemini_service.generate_embedding(question)

        search_results = qdrant_service.search(
            embedding_response.embedding
        )

        prompt = build_rag_prompt(
            question,
            search_results,
        )

        return gemini_service.generate_answer(prompt)


ask_service = AskService()