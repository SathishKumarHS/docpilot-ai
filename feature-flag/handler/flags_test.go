package handler

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestFlagsHandler_ServeHTTP(t *testing.T) {
	flags := map[string]any{
		"limits": map[string]any{
			"anonymous": map[string]any{
				"max-documents": float64(3),
			},
		},
	}
	h := NewFlagsHandler(flags)

	req := httptest.NewRequest("GET", "/flags", nil)
	rec := httptest.NewRecorder()
	h.ServeHTTP(rec, req)

	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d", rec.Code)
	}
	if ct := rec.Header().Get("Content-Type"); ct != "application/json" {
		t.Fatalf("expected application/json, got %s", ct)
	}

	var body map[string]any
	if err := json.NewDecoder(rec.Body).Decode(&body); err != nil {
		t.Fatalf("failed to decode body: %v", err)
	}

	limits := body["limits"].(map[string]any)
	anon := limits["anonymous"].(map[string]any)
	if anon["max-documents"] != float64(3) {
		t.Fatalf("expected max-documents=3, got %v", anon["max-documents"])
	}
}

func TestFlagsHandler_Empty(t *testing.T) {
	h := NewFlagsHandler(map[string]any{})
	req := httptest.NewRequest("GET", "/flags", nil)
	rec := httptest.NewRecorder()
	h.ServeHTTP(rec, req)

	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d", rec.Code)
	}

	var body map[string]any
	if err := json.NewDecoder(rec.Body).Decode(&body); err != nil {
		t.Fatalf("failed to decode body: %v", err)
	}
	if len(body) != 0 {
		t.Fatalf("expected empty object, got %v", body)
	}
}
