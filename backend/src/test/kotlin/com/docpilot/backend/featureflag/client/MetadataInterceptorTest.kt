package com.docpilot.backend.featureflag.client

import io.grpc.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class MetadataInterceptorTest {

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `intercept adds metadata header`() {
        val interceptor = MetadataInterceptor("x-api-key", "test-key")
        val headers = Metadata()
        val methodDescriptor = Mockito.mock(MethodDescriptor::class.java)
        val callOptions = Mockito.mock(CallOptions::class.java)
        val channel = Mockito.mock(Channel::class.java)
        val mockCall = Mockito.mock(ClientCall::class.java)
        val listener = Mockito.mock(ClientCall.Listener::class.java)

        Mockito.`when`(channel.newCall(methodDescriptor, callOptions)).thenReturn(mockCall)

        val interceptedCall = interceptor.interceptCall(methodDescriptor, callOptions, channel)

        (interceptedCall as io.grpc.ClientCall<Any, Any>).start(
            listener as io.grpc.ClientCall.Listener<Any>,
            headers
        )

        assert(headers.get(Metadata.Key.of("x-api-key", Metadata.ASCII_STRING_MARSHALLER)) == "test-key")
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `multiple interceptors each add their header`() {
        val methodDescriptor = Mockito.mock(MethodDescriptor::class.java)
        val callOptions = Mockito.mock(CallOptions::class.java)
        val channel = Mockito.mock(Channel::class.java)
        val mockCall = Mockito.mock(ClientCall::class.java)
        val listener = Mockito.mock(ClientCall.Listener::class.java)

        Mockito.`when`(channel.newCall(methodDescriptor, callOptions)).thenReturn(mockCall)

        val i1 = MetadataInterceptor("key1", "val1")
        val i2 = MetadataInterceptor("key2", "val2")

        // Chain: i2 wraps the call from i1, which wraps the channel's call
        val call1 = i1.interceptCall(methodDescriptor, callOptions, channel)
        // Create a channel adapter that returns call1 from newCall, so i2 wraps it
        val wrappedChannel = object : Channel() {
            override fun <RequestT, ResponseT> newCall(
                method: MethodDescriptor<RequestT, ResponseT>,
                callOptions: CallOptions,
            ): ClientCall<RequestT, ResponseT> {
                @Suppress("UNCHECKED_CAST")
                return call1 as ClientCall<RequestT, ResponseT>
            }
            override fun authority(): String = "test"
        }
        val chainedCall = i2.interceptCall(methodDescriptor, callOptions, wrappedChannel)

        val headers = Metadata()
        (chainedCall as io.grpc.ClientCall<Any, Any>).start(
            listener as io.grpc.ClientCall.Listener<Any>,
            headers
        )

        assert(headers.get(Metadata.Key.of("key1", Metadata.ASCII_STRING_MARSHALLER)) == "val1") {
            "Expected key1=val1 but got ${headers.get(Metadata.Key.of("key1", Metadata.ASCII_STRING_MARSHALLER))}"
        }
        assert(headers.get(Metadata.Key.of("key2", Metadata.ASCII_STRING_MARSHALLER)) == "val2") {
            "Expected key2=val2 but got ${headers.get(Metadata.Key.of("key2", Metadata.ASCII_STRING_MARSHALLER))}"
        }
    }
}
