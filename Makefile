.PHONY: unit-test e2e-test proto-gen clean docker-up docker-down run-ui

PROTO_DIR = shared/proto
AI_WORKER_GRPC_DIR = ai-worker/app/grpc

proto-gen: proto-gen-go proto-gen-kotlin proto-gen-python

proto-gen-go:
	export PATH="$$PATH:$$(go env GOPATH)/bin" && protoc --proto_path=$(PROTO_DIR) \
		--go_out=feature-flag/grpc --go_opt=paths=source_relative \
		--go-grpc_out=feature-flag/grpc --go-grpc_opt=paths=source_relative \
		$(PROTO_DIR)/featureflag.proto

proto-gen-kotlin:
	cd backend && ./gradlew generateProto

proto-gen-python:
	cd ai-worker && python3 -m venv .venv && . .venv/bin/activate && pip install grpcio-tools && python3 -m grpc_tools.protoc \
		--proto_path=../$(PROTO_DIR) \
		--python_out=app/grpc \
		--grpc_python_out=app/grpc \
		../$(PROTO_DIR)/aiworker.proto
	sed -i '' 's/import aiworker_pb2 as/from app.grpc import aiworker_pb2 as/' $(AI_WORKER_GRPC_DIR)/aiworker_pb2_grpc.py

docker-build:
	docker-compose build backend ai-worker feature-flag

docker-up: docker-build
	docker-compose up -d

docker-up-no-build:
	docker-compose up -d

docker-down:
	docker-compose down

unit-test: proto-gen-python
	cd feature-flag && go test -v ./...
	cd backend && ./gradlew test
	cd ai-worker && python3 -m venv .venv && . .venv/bin/activate && pip install -r requirements.txt && PYTHONPATH=. pytest tests/

e2e-test: docker-build docker-up-no-build
	export $$(grep '^SERVICE_API_KEY=' .env | xargs) && cd feature-flag && go test -tags=e2e -v ./test/
	export $$(grep -E '^(SERVICE_API_KEY|JWT_SECRET)=' .env | xargs) && cd backend && ./gradlew cleanE2eTest e2eTest

run-ui:
	cd web && npm run dev

clean:
	docker-compose down --volumes --remove-orphans
	rm -f feature-flag/feature-flag
