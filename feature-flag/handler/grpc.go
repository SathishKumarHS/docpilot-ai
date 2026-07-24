package handler

import (
	"context"
	"fmt"

	pb "github.com/docpilot/feature-flag/grpc"
)

type GrpcFlagsHandler struct {
	pb.UnimplementedFeatureFlagServiceServer
	flags map[string]any
}

func NewGrpcFlagsHandler(flags map[string]any) *GrpcFlagsHandler {
	return &GrpcFlagsHandler{flags: flags}
}

func (h *GrpcFlagsHandler) GetFlags(ctx context.Context, req *pb.GetFlagsRequest) (*pb.GetFlagsResponse, error) {
	return &pb.GetFlagsResponse{Flags: flatten(h.flags, "")}, nil
}

func flatten(source map[string]any, prefix string) map[string]string {
	result := make(map[string]string, len(source))
	for key, value := range source {
		fullKey := key
		if prefix != "" {
			fullKey = prefix + "." + key
		}
		switch v := value.(type) {
		case map[string]any:
			for k, val := range flatten(v, fullKey) {
				result[k] = val
			}
		default:
			result[fullKey] = fmt.Sprint(value)
		}
	}
	return result
}
