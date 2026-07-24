from app.prompts.rag_prompt import build_rag_prompt
from app.models.search import SearchResult


def test_build_rag_prompt_with_results():
    results = [
        SearchResult(score=0.95, document_id="d1", chunk_index=0, text="first chunk"),
        SearchResult(score=0.80, document_id="d1", chunk_index=1, text="second chunk"),
    ]
    prompt = build_rag_prompt("test question", results)

    assert "first chunk" in prompt
    assert "second chunk" in prompt
    assert "test question" in prompt
    assert "Context:" in prompt
    assert "Question:" in prompt
    assert "Answer:" in prompt


def test_build_rag_prompt_empty_results():
    prompt = build_rag_prompt("any question", [])

    assert "Context:\n\n\nQuestion:" in prompt
    assert "any question" in prompt
