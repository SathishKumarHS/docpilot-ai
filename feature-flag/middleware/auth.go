package middleware

import (
	"net/http"
)

func ServiceKeyMiddleware(next http.Handler, expectedKey string) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.URL.Path == "/health" {
			next.ServeHTTP(w, r)
			return
		}

		key := r.Header.Get("X-Service-Key")
		if key == "" || key != expectedKey {
			http.Error(w, `{"error":"Invalid or missing service key"}`, http.StatusUnauthorized)
			return
		}

		next.ServeHTTP(w, r)
	})
}
