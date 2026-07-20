.PHONY: docker-up docker-down run-docpilot-ai web-run clean

docker-up:
	docker-compose up -d

docker-down:
	docker-compose down

run-docpilot-ai: docker-up
	@echo "Starting Web UI..."
	cd web && npm run dev

web-run:
	cd web && npm run dev

clean:
	docker-compose down --volumes --remove-orphans
	rm -rf uploads
