.PHONY: docker-up docker-down run-docpilot-ai web-run clean build tidy test-feature-flag e2e-test-feature-flag

docker-up:
	docker-compose up -d

docker-down:
	docker-compose down

run-docpilot-ai: docker-up
	@echo "Starting Web UI..."
	cd web && npm run dev

web-run:
	cd web && npm run dev

build:
	cd feature-flag && go build -o feature-flag .

tidy:
	cd feature-flag && go mod tidy

e2e-test-feature-flag: docker-up
	cd feature-flag && go test -tags=e2e -v ./test/

e2e-test: e2e-test-feature-flag

clean:
	docker-compose down --volumes --remove-orphans
	rm -rf uploads
	rm -f feature-flag/feature-flag
