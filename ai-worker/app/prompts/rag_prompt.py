from app.models.search import SearchResult


def build_rag_prompt(
    question: str,
    search_results: list[SearchResult],
) -> str:
    context = "\n\n".join(
        result.text for result in search_results
    )

    return f"""
You are an AI assistant that answers questions using ONLY the provided context.

Instructions:
- Use only the context below.
- If the answer is not present in the context, say:
  "I couldn't find that information in the uploaded documents."
- Do not make up facts.
- Keep the answer concise and accurate.

Context:
{context}

Question:
{question}

Answer:
"""