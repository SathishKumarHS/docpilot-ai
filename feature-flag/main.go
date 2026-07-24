package main

import (
	"log"
	"net"
	"net/http"
	"os"

	"github.com/docpilot/feature-flag/config"
	"github.com/docpilot/feature-flag/handler"
	"github.com/docpilot/feature-flag/middleware"
	pb "github.com/docpilot/feature-flag/grpc"
	"google.golang.org/grpc"
	"google.golang.org/grpc/reflection"
)

func getPort() string {
	if p := os.Getenv("PORT"); p != "" {
		return ":" + p
	}
	return ":8090"
}

func getGrpcPort() string {
	if p := os.Getenv("GRPC_PORT"); p != "" {
		return ":" + p
	}
	return ":9090"
}

func main() {
	cfg, err := config.Load()
	if err != nil {
		log.Fatalf("failed to load config: %v", err)
	}

	serviceKey := os.Getenv("SERVICE_API_KEY")

	// HTTP server (health + backward compat)
	mux := http.NewServeMux()
	mux.Handle("GET /flags", middleware.ServiceKeyMiddleware(handler.NewFlagsHandler(cfg.Flags), serviceKey))
	mux.Handle("GET /health", handler.NewHealthHandler())

	httpAddr := getPort()
	go func() {
		log.Printf("HTTP server starting on %s", httpAddr)
		if err := http.ListenAndServe(httpAddr, mux); err != nil {
			log.Fatalf("HTTP server failed: %v", err)
		}
	}()

	// gRPC server
	grpcAddr := getGrpcPort()
	lis, err := net.Listen("tcp", grpcAddr)
	if err != nil {
		log.Fatalf("failed to listen on %s: %v", grpcAddr, err)
	}

	grpcServer := grpc.NewServer(
		grpc.UnaryInterceptor(middleware.GrpcServiceKeyInterceptor(serviceKey)),
	)
	pb.RegisterFeatureFlagServiceServer(grpcServer, handler.NewGrpcFlagsHandler(cfg.Flags))
	reflection.Register(grpcServer)

	log.Printf("gRPC server starting on %s", grpcAddr)
	if err := grpcServer.Serve(lis); err != nil {
		log.Fatalf("gRPC server failed: %v", err)
	}
}
