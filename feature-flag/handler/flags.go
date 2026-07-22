package handler

import (
	"encoding/json"
	"net/http"
)

type FlagsHandler struct {
	flags map[string]any
}

func NewFlagsHandler(flags map[string]any) *FlagsHandler {
	return &FlagsHandler{flags: flags}
}

func (h *FlagsHandler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(h.flags)
}
