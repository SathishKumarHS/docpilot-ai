.PHONY: docker-up docker-down run-docpilot-ai web-run web-install build build-backend tidy test-feature-flag e2e-test-feature-flag e2e-test-backend e2e-test clean

web-install:
	cd web && npm install

run-docpilot-ai: docker-up web-install
	@echo "Starting Web UI..."
	cd web && npm run dev

web-run: web-install
	cd web && npm run dev

build:
	cd feature-flag && go build -o feature-flag .

build-backend:
	cd backend && ./gradlew bootJar

tidy:
	cd feature-flag && go mod tidy

docker-up: build build-backend
	docker-compose up -d --build

docker-down:
	docker-compose down

test-feature-flag:
	cd feature-flag && go test -v ./...

e2e-test-feature-flag: docker-up
	cd feature-flag && go test -tags=e2e -v ./test/

e2e-test-backend: docker-up
	cd backend && ./gradlew cleanE2eTest e2eTest

e2e-test: e2e-test-feature-flag e2e-test-backend

clean:
	docker-compose down --volumes --remove-orphans
	rm -f feature-flag/feature-flag
