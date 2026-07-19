package com.docpilot.backend.aiworker.exception

class AiWorkerException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)