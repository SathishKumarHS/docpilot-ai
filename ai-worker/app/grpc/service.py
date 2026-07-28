import asyncio
from uuid import UUID

import grpc

from app.grpc import aiworker_pb2, aiworker_pb2_grpc
from app.grpc.aiworker_pb2 import MessageRole
from app.models.index import ChunkRequest, IndexDocumentRequest as IndexRequestModel
from app.services.index_service import index_service
from app.services.ask_service import ask_service
from app.services.document_service import document_service


def _get_client_id(context):
    metadata = dict(context.invocation_metadata())
    return metadata.get("x-client-id", "")


def _authenticated(context):
    metadata = dict(context.invocation_metadata())
    from app.config.settings import settings
    key = metadata.get("x-service-key", "")
    if not key or key != settings.service_api_key:
        context.abort(grpc.StatusCode.UNAUTHENTICATED, "Invalid or missing service key")
        return False
    return True


class AiWorkerGrpcServicer(aiworker_pb2_grpc.AiWorkerServiceServicer):

    def IndexDocument(self, request, context):
        if not _authenticated(context):
            return aiworker_pb2.IndexDocumentResponse(indexed_chunks=0)

        client_id = _get_client_id(context)

        chunks = [
            ChunkRequest(chunk_id=UUID(c.chunk_id), chunk_index=c.chunk_index, text=c.text)
            for c in request.chunks
        ]
        model_request = IndexRequestModel(
            document_id=UUID(request.document_id),
            chunks=chunks,
        )
        result = index_service.index_document(model_request, client_id)
        return aiworker_pb2.IndexDocumentResponse(indexed_chunks=result.indexed_chunks)

    def Ask(self, request, context):
        if not _authenticated(context):
            return aiworker_pb2.AskResponse(answer="")

        client_id = _get_client_id(context)

        chat_history = [(MessageRole.Name(m.role).lower(), m.content) for m in request.chat_history]

        answer = ask_service.ask(
            question=request.question,
            client_id=client_id,
            document_id=request.document_id or None,
            chat_history=chat_history,
        )
        return aiworker_pb2.AskResponse(answer=answer)

    def DeleteDocument(self, request, context):
        if not _authenticated(context):
            return aiworker_pb2.DeleteDocumentResponse(deleted=False)

        client_id = _get_client_id(context)

        loop = asyncio.new_event_loop()
        try:
            loop.run_until_complete(
                document_service.delete_document(UUID(request.document_id), client_id)
            )
        finally:
            loop.close()

        return aiworker_pb2.DeleteDocumentResponse(deleted=True)


async def serve_grpc(port: int):
    server = grpc.aio.server()
    aiworker_pb2_grpc.add_AiWorkerServiceServicer_to_server(
        AiWorkerGrpcServicer(), server
    )
    server.add_insecure_port(f"[::]:{port}")
    await server.start()
    print(f"gRPC server listening on port {port}")
    await server.wait_for_termination()
