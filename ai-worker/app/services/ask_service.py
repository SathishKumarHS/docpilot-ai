from collections.abc import Generator

from app.prompts.rag_prompt import build_rag_prompt, build_suggestions_prompt, build_summary_prompt, parse_suggestions
from app.services.gemini_service import gemini_service
from app.services.qdrant_service import qdrant_service


class AskService:

    def ask(
        self,
        question: str,
        client_id: str,
        document_id: str | None = None,
        chat_history: list[tuple[str, str]] | None = None,
    ) -> str:
        embedding_response = gemini_service.generate_embedding(question)

        search_results = qdrant_service.search(
            embedding_response.embedding,
            client_id=client_id,
            document_id=document_id,
            limit=20,
        )

        prompt = build_rag_prompt(
            question,
            search_results,
            chat_history,
        )

        return gemini_service.generate_answer(prompt)

    def ask_stream(
        self,
        question: str,
        client_id: str,
        document_id: str | None = None,
        chat_history: list[tuple[str, str]] | None = None,
    ) -> Generator[str, None, None]:
        embedding_response = gemini_service.generate_embedding(question)

        search_results = qdrant_service.search(
            embedding_response.embedding,
            client_id=client_id,
            document_id=document_id,
            limit=20,
        )

        prompt = build_rag_prompt(
            question,
            search_results,
            chat_history,
        )

        yield from gemini_service.generate_answer_stream(prompt)

    def suggest_questions(
        self,
        client_id: str,
        document_id: str | None = None,
        chat_history: list[tuple[str, str]] | None = None,
    ) -> list[str]:
        last_question = ""
        if chat_history:
            for role, content in reversed(chat_history):
                if role == "user":
                    last_question = content
                    break

        if last_question:
            embedding_response = gemini_service.generate_embedding(last_question)
            search_results = qdrant_service.search(
                embedding_response.embedding,
                client_id=client_id,
                document_id=document_id,
                limit=20,
            )
        else:
            search_results = []

        prompt = build_suggestions_prompt(search_results, chat_history)
        text = gemini_service.generate_answer(prompt)
        return parse_suggestions(text)


    def summarize(self, chunks: list[str]) -> str:
        prompt = build_summary_prompt(chunks)
        return gemini_service.generate_answer(prompt)


ask_service = AskService()
