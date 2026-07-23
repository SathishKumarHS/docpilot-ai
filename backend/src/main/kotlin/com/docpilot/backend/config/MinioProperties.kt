package com.docpilot.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "minio")
class MinioProperties {
    lateinit var url: String
    lateinit var accessKey: String
    lateinit var secretKey: String
    var bucket: String = "docpilot"
}
