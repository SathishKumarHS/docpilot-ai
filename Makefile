.PHONY: docker-up docker-down run-docpilot-ai web-run

ifneq (,$(wildcard .env))
    include .env
    export
endif

docker-up:
	docker compose up -d

docker-down:
	docker compose down

run-docpilot-ai: docker-up
	@echo "Starting AI Worker..."
	cd ai-worker && uvicorn app.main:app --reload --host 0.0.0.0 --port 8000 &
	@sleep 3
	@echo "Starting Backend..."
	cd backend && ./gradlew bootRun &
	@sleep 8
	@echo "Starting Web UI..."
	cd web && npm run dev

web-run:
	cd web && npm run dev
