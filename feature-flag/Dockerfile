FROM golang:1.24-alpine AS builder

WORKDIR /build

COPY go.mod go.sum ./
RUN go mod download

COPY . .
RUN CGO_ENABLED=0 go build -o feature-flag .

FROM alpine:3.21

WORKDIR /app

COPY --from=builder /build/feature-flag .
COPY --from=builder /build/data ./data

EXPOSE 8090

CMD ["./feature-flag"]
