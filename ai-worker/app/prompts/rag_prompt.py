from app.models.search import SearchResult


def build_rag_prompt(
    question: str,
    search_results: list[SearchResult],
    chat_history: list[tuple[str, str]] | None = None,
) -> str:
    context = "\n\n".join(
        result.text for result in search_results
    )

    history_block = ""
    if chat_history:
        formatted = "\n".join(
            f"{'User' if role == 'user' else 'Assistant'}: {content}"
            for role, content in chat_history[-10:]
        )
        history_block = f"""
Conversation history:
{formatted}

"""

    return f"""
You are an AI assistant that answers questions using the provided context from uploaded documents.

Instructions:
- Use the context below to answer the question.
- If the context contains relevant information, answer naturally using it.
- If you are asked to summarize, use the provided excerpts to create a concise summary.
- If the context has no relevant information at all, say:
  "I couldn't find that information in the uploaded documents."
- Do not make up facts. Only answer based on what is in the context.
- Keep the answer concise and accurate.
- Use the conversation history for context about previous questions.

{history_block}Context:
{context}

Question:
{question}

Answer:
"""