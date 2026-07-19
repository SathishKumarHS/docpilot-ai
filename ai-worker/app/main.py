from fastapi import FastAPI

app = FastAPI(
    title="DocPilot AI Worker",
    version="1.0.0"
)


@app.get("/health")
def health():
    return {
        "status": "UP",
        "service": "ai-worker"
    }