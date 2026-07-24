.PHONY: docker-up docker-down run-docpilot-ai web-run web-install build build-backend tidy test-feature-flag e2e-test-feature-flag e2e-test-backend e2e-test clean build-ai-worker build-feature-flag build-backend-docker rebuild-ai-worker rebuild-all logs proto-gen proto-gen-go proto-gen-kotlin

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

docker-build:
	docker-compose build backend ai-worker feature-flag

build-ai-worker:
	docker-compose build ai-worker

build-feature-flag:
	docker-compose build feature-flag

build-backend-docker:
	docker-compose build backend

rebuild-ai-worker: build-ai-worker docker-up

rebuild-all:
	docker-compose build --no-cache
	docker-compose up -d

tidy:
	cd feature-flag && go mod tidy

docker-up: docker-build
	docker-compose up -d

docker-up-no-build:
	docker-compose up -d

docker-down:
	docker-compose down

logs:
	docker-compose logs -f

test-feature-flag:
	cd feature-flag && go test -v ./...

e2e-test-feature-flag: docker-build docker-up-no-build
	cd feature-flag && go test -tags=e2e -v ./test/

e2e-test-backend: docker-build docker-up-no-build
	cd backend && ./gradlew cleanE2eTest e2eTest

e2e-test: e2e-test-feature-flag e2e-test-backend

PROTO_DIR = feature-flag/proto

proto-gen: proto-gen-go proto-gen-kotlin

proto-gen-go:
	protoc --proto_path=$(PROTO_DIR) \
		--go_out=$(PROTO_DIR) --go_opt=paths=source_relative \
		--go-grpc_out=$(PROTO_DIR) --go-grpc_opt=paths=source_relative \
		$(PROTO_DIR)/featureflag.proto

proto-gen-kotlin:
	cd backend && ./gradlew generateProto

clean:
	docker-compose down --volumes --remove-orphans
	rm -f feature-flag/feature-flag
