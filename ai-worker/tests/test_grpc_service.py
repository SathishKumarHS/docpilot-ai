import pytest
from uuid import UUID
from unittest.mock import MagicMock, patch

from app.config.settings import settings as app_settings
from app.grpc import aiworker_pb2
from app.grpc.service import AiWorkerGrpcServicer, _authenticated, _get_client_id


class FakeContext:
    def __init__(self, metadata=None):
        self._metadata = metadata or {}
        self._aborted = False
        self._code = None
        self._details = None

    def invocation_metadata(self):
        return list(self._metadata.items())

    def abort(self, code, details):
        self._aborted = True
        self._code = code
        self._details = details


class TestGrpcServicer:

    def test_index_document_success(self):
        servicer = AiWorkerGrpcServicer()

        proto_request = aiworker_pb2.IndexDocumentRequest(
            document_id="550e8400-e29b-41d4-a716-446655440000",
            chunks=[
                aiworker_pb2.Chunk(chunk_id="660e8400-e29b-41d4-a716-446655440001", chunk_index=0, text="hello"),
            ],
        )

        context = FakeContext(metadata={
            "x-service-key": "test-key",
            "x-client-id": "client-1",
        })

        mock_index = MagicMock()
        mock_index.index_document.return_value.indexed_chunks = 1

        original = app_settings.service_api_key
        app_settings.service_api_key = "test-key"
        try:
            with patch("app.grpc.service.index_service", mock_index):
                response = servicer.IndexDocument(proto_request, context)
        finally:
            app_settings.service_api_key = original

        assert response.indexed_chunks == 1
        assert not context._aborted

    def test_index_document_unauthenticated(self):
        servicer = AiWorkerGrpcServicer()

        proto_request = aiworker_pb2.IndexDocumentRequest(
            document_id="550e8400-e29b-41d4-a716-446655440000",
        )
        context = FakeContext(metadata={})

        original = app_settings.service_api_key
        app_settings.service_api_key = "test-key"
        try:
            response = servicer.IndexDocument(proto_request, context)
        finally:
            app_settings.service_api_key = original

        assert response.indexed_chunks == 0
        assert context._aborted

    def test_ask_success(self):
        servicer = AiWorkerGrpcServicer()

        proto_request = aiworker_pb2.AskRequest(question="test question")

        context = FakeContext(metadata={
            "x-service-key": "test-key",
            "x-client-id": "client-1",
        })

        mock_ask = MagicMock()
        mock_ask.ask.return_value = "the answer"

        original = app_settings.service_api_key
        app_settings.service_api_key = "test-key"
        try:
            with patch("app.grpc.service.ask_service", mock_ask):
                response = servicer.Ask(proto_request, context)
        finally:
            app_settings.service_api_key = original

        assert response.answer == "the answer"
        mock_ask.ask.assert_called_once_with(
            question="test question",
            client_id="client-1",
            document_id=None,
        )

    def test_ask_with_document_id(self):
        servicer = AiWorkerGrpcServicer()

        proto_request = aiworker_pb2.AskRequest(
            question="test question",
            document_id="doc-1",
        )

        context = FakeContext(metadata={
            "x-service-key": "test-key",
            "x-client-id": "client-1",
        })

        mock_ask = MagicMock()
        mock_ask.ask.return_value = "the answer"

        original = app_settings.service_api_key
        app_settings.service_api_key = "test-key"
        try:
            with patch("app.grpc.service.ask_service", mock_ask):
                response = servicer.Ask(proto_request, context)
        finally:
            app_settings.service_api_key = original

        assert response.answer == "the answer"
        mock_ask.ask.assert_called_once_with(
            question="test question",
            client_id="client-1",
            document_id="doc-1",
        )

    def test_ask_unauthenticated(self):
        servicer = AiWorkerGrpcServicer()

        proto_request = aiworker_pb2.AskRequest(question="test")
        context = FakeContext(metadata={})

        original = app_settings.service_api_key
        app_settings.service_api_key = "test-key"
        try:
            response = servicer.Ask(proto_request, context)
        finally:
            app_settings.service_api_key = original

        assert response.answer == ""
        assert context._aborted

    def test_delete_document_success(self):
        servicer = AiWorkerGrpcServicer()

        proto_request = aiworker_pb2.DeleteDocumentRequest(
            document_id="550e8400-e29b-41d4-a716-446655440000",
        )

        context = FakeContext(metadata={
            "x-service-key": "test-key",
            "x-client-id": "client-1",
        })

        mock_doc = MagicMock()
        mock_delete = MagicMock()

        original = app_settings.service_api_key
        app_settings.service_api_key = "test-key"
        try:
            with (
                patch("app.grpc.service.document_service", mock_doc),
                patch("asyncio.new_event_loop") as mock_loop,
            ):
                mock_loop.return_value = mock_delete
                mock_delete.run_until_complete.return_value = None
                response = servicer.DeleteDocument(proto_request, context)
        finally:
            app_settings.service_api_key = original

        assert response.deleted
        assert not context._aborted


class TestHelpers:

    def test_get_client_id_present(self):
        context = FakeContext(metadata={"x-client-id": "my-client"})
        assert _get_client_id(context) == "my-client"

    def test_get_client_id_missing(self):
        context = FakeContext(metadata={})
        assert _get_client_id(context) == ""

    def test_authenticated_valid(self):
        context = FakeContext(metadata={"x-service-key": "correct"})
        original = app_settings.service_api_key
        app_settings.service_api_key = "correct"
        try:
            result = _authenticated(context)
        finally:
            app_settings.service_api_key = original
        assert result is True
        assert not context._aborted

    def test_authenticated_invalid(self):
        context = FakeContext(metadata={"x-service-key": "wrong"})
        original = app_settings.service_api_key
        app_settings.service_api_key = "correct"
        try:
            result = _authenticated(context)
        finally:
            app_settings.service_api_key = original
        assert result is False
        assert context._aborted
        assert context._code.name == "UNAUTHENTICATED"

    def test_authenticated_missing(self):
        context = FakeContext(metadata={})
        original = app_settings.service_api_key
        app_settings.service_api_key = "correct"
        try:
            result = _authenticated(context)
        finally:
            app_settings.service_api_key = original
        assert result is False
        assert context._aborted
