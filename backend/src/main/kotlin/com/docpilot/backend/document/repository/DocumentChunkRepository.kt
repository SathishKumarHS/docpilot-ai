package com.docpilot.backend.document.repository

import com.docpilot.backend.document.entity.DocumentChunkEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface DocumentChunkRepository :
    JpaRepository<DocumentChunkEntity, UUID>