package main

import (
	"log"
	"net/http"
	"os"

	"github.com/docpilot/feature-flag/config"
	"github.com/docpilot/feature-flag/handler"
)

func getPort() string {
	if p := os.Getenv("PORT"); p != "" {
		return ":" + p
	}
	return ":8090"
}

func main() {
	cfg, err := config.Load()
	if err != nil {
		log.Fatalf("failed to load config: %v", err)
	}

	mux := http.NewServeMux()
	mux.Handle("GET /flags", handler.NewFlagsHandler(cfg.Flags))
	mux.Handle("GET /health", handler.NewHealthHandler())

	addr := getPort()
	log.Printf("feature-flag service starting on %s", addr)
	log.Fatal(http.ListenAndServe(addr, mux))
}
