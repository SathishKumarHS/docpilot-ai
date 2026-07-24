//go:build e2e

package test

import (
	"encoding/json"
	"net/http"
	"os"
	"testing"
)

const defaultAddr = "http://localhost:8090"

func getAddr() string {
	if addr := os.Getenv("FEATURE_FLAG_ADDR"); addr != "" {
		return addr
	}
	return defaultAddr
}

func getServiceKey() string {
	if key := os.Getenv("SERVICE_API_KEY"); key != "" {
		return key
	}
	return "test-service-api-key"
}

func TestE2E_Flags(t *testing.T) {
	req, _ := http.NewRequest("GET", getAddr()+"/flags", nil)
	req.Header.Set("X-Service-Key", getServiceKey())
	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		t.Fatalf("GET /flags: %v", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		t.Fatalf("expected 200, got %d", resp.StatusCode)
	}

	var body map[string]any
	if err := json.NewDecoder(resp.Body).Decode(&body); err != nil {
		t.Fatalf("decode body: %v", err)
	}

	limits, ok := body["limits"].(map[string]any)
	if !ok {
		t.Fatal("missing 'limits' key")
	}
	anon, ok := limits["anonymous"].(map[string]any)
	if !ok {
		t.Fatal("missing 'anonymous' under limits")
	}
	if anon["max-documents"] == nil {
		t.Fatal("expected max-documents")
	}
}

func TestE2E_Health(t *testing.T) {
	resp, err := http.Get(getAddr() + "/health")
	if err != nil {
		t.Fatalf("GET /health: %v", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		t.Fatalf("expected 200, got %d", resp.StatusCode)
	}
}
