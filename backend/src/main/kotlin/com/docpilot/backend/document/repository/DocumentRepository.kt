package com.docpilot.backend.document.repository

import com.docpilot.backend.document.entity.DocumentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface DocumentRepository :
    JpaRepository<DocumentEntity, UUID>