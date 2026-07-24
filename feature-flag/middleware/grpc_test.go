package middleware

import (
	"context"
	"testing"

	"google.golang.org/grpc"
	"google.golang.org/grpc/metadata"
)

type fakeHandler struct {
	called bool
}

func (f *fakeHandler) handle(ctx context.Context, req any) (any, error) {
	f.called = true
	return "response", nil
}

func TestGrpcServiceKeyInterceptor_ValidKey(t *testing.T) {
	interceptor := GrpcServiceKeyInterceptor("secret")
	ctx := metadata.NewIncomingContext(context.Background(), metadata.Pairs("x-service-key", "secret"))
	fh := &fakeHandler{}

	resp, err := interceptor(ctx, "request", &grpc.UnaryServerInfo{}, fh.handle)

	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if !fh.called {
		t.Fatal("handler should have been called")
	}
	if resp != "response" {
		t.Fatalf("expected 'response', got %v", resp)
	}
}

func TestGrpcServiceKeyInterceptor_MissingKey(t *testing.T) {
	interceptor := GrpcServiceKeyInterceptor("secret")
	ctx := context.Background()
	fh := &fakeHandler{}

	_, err := interceptor(ctx, "request", &grpc.UnaryServerInfo{}, fh.handle)

	if err == nil {
		t.Fatal("expected error, got nil")
	}
	if fh.called {
		t.Fatal("handler should not have been called")
	}
}

func TestGrpcServiceKeyInterceptor_WrongKey(t *testing.T) {
	interceptor := GrpcServiceKeyInterceptor("secret")
	ctx := metadata.NewIncomingContext(context.Background(), metadata.Pairs("x-service-key", "wrong"))
	fh := &fakeHandler{}

	_, err := interceptor(ctx, "request", &grpc.UnaryServerInfo{}, fh.handle)

	if err == nil {
		t.Fatal("expected error, got nil")
	}
	if fh.called {
		t.Fatal("handler should not have been called")
	}
}

func TestGrpcServiceKeyInterceptor_MultipleKeys(t *testing.T) {
	interceptor := GrpcServiceKeyInterceptor("secret")
	ctx := metadata.NewIncomingContext(context.Background(),
		metadata.Pairs("x-service-key", "wrong", "x-service-key", "secret"))
	fh := &fakeHandler{}

	_, err := interceptor(ctx, "request", &grpc.UnaryServerInfo{}, fh.handle)

	if err == nil {
		t.Fatal("expected error, got nil")
	}
}

func TestGrpcServiceKeyInterceptor_EmptyExpectedKey(t *testing.T) {
	interceptor := GrpcServiceKeyInterceptor("")
	ctx := context.Background()
	fh := &fakeHandler{}

	resp, err := interceptor(ctx, "request", &grpc.UnaryServerInfo{}, fh.handle)

	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if !fh.called {
		t.Fatal("handler should have been called")
	}
	if resp != "response" {
		t.Fatalf("expected 'response', got %v", resp)
	}
}

func TestGrpcServiceKeyInterceptor_EmptyValueKey(t *testing.T) {
	interceptor := GrpcServiceKeyInterceptor("secret")
	ctx := metadata.NewIncomingContext(context.Background(), metadata.Pairs("x-service-key", ""))
	fh := &fakeHandler{}

	_, err := interceptor(ctx, "request", &grpc.UnaryServerInfo{}, fh.handle)

	if err == nil {
		t.Fatal("expected error, got nil")
	}
}
