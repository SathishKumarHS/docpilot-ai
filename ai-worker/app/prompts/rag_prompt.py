import re

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


def build_suggestions_prompt(
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
            for role, content in chat_history[-5:]
        )
        history_block = f"""
Conversation history:
{formatted}

"""

    return f"""
You are an AI assistant that suggests follow-up questions about the user's documents.

Review the document excerpts and conversation below. Suggest 3-5 questions that:
- Can be answered from the document excerpts provided
- Dig deeper into topics the user has already asked about
- Cover important points the user hasn't asked about yet

Return each question on a new line, numbered 1-5. Do not include any other text.

{history_block}Document excerpts:
{context if context else "(No document content available — suggest general questions about what the user might want to know about their documents.)"}

Suggested questions:
"""


def build_summary_prompt(chunks: list[str]) -> str:
    content = "\n\n".join(chunks)

    return f"""
Summarize the following document content in 3-5 sentences. Capture the main topic, key points, and any important conclusions.

Document content:
{content}

Summary:
"""


def parse_suggestions(text: str) -> list[str]:
    questions = []
    for line in text.strip().split("\n"):
        line = line.strip()
        line = re.sub(r"^\d+[\.\)]\s*", "", line).strip()
        if line and not line.startswith("#"):
            questions.append(line)
    return questions[:5]