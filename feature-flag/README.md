# DocPilot Feature Flag

Go service that serves tier-based feature flags (anonymous vs. registered user limits). Supports both HTTP and gRPC.

## Tech

- **Language:** Go 1.25
- **Config:** Viper (YAML-driven)
- **Transport:** HTTP + gRPC (dual protocol)

## Project structure

```
feature-flag/
├── config/       # Config loader
├── data/         # flags.yml (tier limits per feature)
├── grpc/         # Generated protobuf stubs
├── handler/      # HTTP handlers + gRPC server
├── middleware/    # Service key auth
└── main.go       # Entry point
```

## Ports

| Port | Protocol | Description |
|------|----------|-------------|
| 8090 | HTTP     | `GET /flags`, `GET /health` |
| 9090 | gRPC    | `GetFlags` RPC |

## Running

```bash
go test ./...

go build -o feature-flag . && ./feature-flag
```

## flags.yml example

```yaml
flags:
  max_documents:
    anonymous: 3
    registered: 50
  max_daily_questions:
    anonymous: 10
    registered: 100
```
