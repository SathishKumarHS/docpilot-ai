package middleware

import (
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestServiceKeyMiddleware_ValidKey(t *testing.T) {
	handler := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
	})

	wrapped := ServiceKeyMiddleware(handler, "secret")
	req := httptest.NewRequest("GET", "/flags", nil)
	req.Header.Set("X-Service-Key", "secret")
	rec := httptest.NewRecorder()

	wrapped.ServeHTTP(rec, req)

	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d", rec.Code)
	}
}

func TestServiceKeyMiddleware_MissingKey(t *testing.T) {
	handler := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		t.Error("handler should not be called")
	})

	wrapped := ServiceKeyMiddleware(handler, "secret")
	req := httptest.NewRequest("GET", "/flags", nil)
	rec := httptest.NewRecorder()

	wrapped.ServeHTTP(rec, req)

	if rec.Code != http.StatusUnauthorized {
		t.Fatalf("expected 401, got %d", rec.Code)
	}
}

func TestServiceKeyMiddleware_WrongKey(t *testing.T) {
	handler := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		t.Error("handler should not be called")
	})

	wrapped := ServiceKeyMiddleware(handler, "secret")
	req := httptest.NewRequest("GET", "/flags", nil)
	req.Header.Set("X-Service-Key", "wrong")
	rec := httptest.NewRecorder()

	wrapped.ServeHTTP(rec, req)

	if rec.Code != http.StatusUnauthorized {
		t.Fatalf("expected 401, got %d", rec.Code)
	}
}

func TestServiceKeyMiddleware_HealthPathBypassesAuth(t *testing.T) {
	called := false
	handler := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		called = true
		w.WriteHeader(http.StatusOK)
	})

	wrapped := ServiceKeyMiddleware(handler, "secret")
	req := httptest.NewRequest("GET", "/health", nil)
	rec := httptest.NewRecorder()

	wrapped.ServeHTTP(rec, req)

	if !called {
		t.Fatal("handler should have been called")
	}
	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d", rec.Code)
	}
}

func TestServiceKeyMiddleware_EmptyExpectedKey(t *testing.T) {
	handler := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
	})

	wrapped := ServiceKeyMiddleware(handler, "")
	req := httptest.NewRequest("GET", "/flags", nil)
	rec := httptest.NewRecorder()

	wrapped.ServeHTTP(rec, req)

	if rec.Code != http.StatusUnauthorized {
		t.Fatalf("expected 401, got %d", rec.Code)
	}
}
