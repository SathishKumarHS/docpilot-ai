package handler

import (
	"context"
	"testing"

	pb "github.com/docpilot/feature-flag/grpc"
)

func TestGrpcFlagsHandler_GetFlags(t *testing.T) {
	flags := map[string]any{
		"limits": map[string]any{
			"anonymous": map[string]any{
				"max-documents": float64(3),
			},
		},
		"string-flag": "value",
	}
	h := NewGrpcFlagsHandler(flags)

	req := &pb.GetFlagsRequest{}
	resp, err := h.GetFlags(context.Background(), req)

	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if resp == nil {
		t.Fatal("expected non-nil response")
	}

	expected := map[string]string{
		"limits.anonymous.max-documents": "3",
		"string-flag":                   "value",
	}
	for k, v := range expected {
		if resp.Flags[k] != v {
			t.Errorf("expected flags[%q] = %q, got %q", k, v, resp.Flags[k])
		}
	}
}

func TestGrpcFlagsHandler_EmptyFlags(t *testing.T) {
	h := NewGrpcFlagsHandler(map[string]any{})

	req := &pb.GetFlagsRequest{}
	resp, err := h.GetFlags(context.Background(), req)

	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if len(resp.Flags) != 0 {
		t.Errorf("expected empty flags, got %v", resp.Flags)
	}
}

func TestGrpcFlagsHandler_NestedFlags(t *testing.T) {
	flags := map[string]any{
		"a": map[string]any{
			"b": map[string]any{
				"c": "deep",
			},
		},
	}
	h := NewGrpcFlagsHandler(flags)

	req := &pb.GetFlagsRequest{}
	resp, err := h.GetFlags(context.Background(), req)

	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if resp.Flags["a.b.c"] != "deep" {
		t.Errorf("expected a.b.c=deep, got %q", resp.Flags["a.b.c"])
	}
	if len(resp.Flags) != 1 {
		t.Errorf("expected 1 flag, got %d", len(resp.Flags))
	}
}
