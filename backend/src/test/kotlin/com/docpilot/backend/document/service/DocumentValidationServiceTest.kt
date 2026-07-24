package com.docpilot.backend.document.service

import com.docpilot.backend.auth.model.OwnerType
import com.docpilot.backend.exception.UnsupportedDocumentSizeException
import com.docpilot.backend.exception.UnsupportedDocumentTypeException
import com.docpilot.backend.featureflag.model.TierLimits
import com.docpilot.backend.featureflag.service.FeatureFlagService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.multipart.MultipartFile
import kotlin.test.assertFailsWith

@ExtendWith(MockitoExtension::class)
class DocumentValidationServiceTest {

    @Mock private lateinit var featureFlagService: FeatureFlagService
    @Mock private lateinit var file: MultipartFile

    @Test
    fun `throws on empty file`() {
        `when`(file.isEmpty).thenReturn(true)

        assertFailsWith<IllegalArgumentException> {
            DocumentValidationService(featureFlagService).validate(file, OwnerType.USER)
        }
    }

    @Test
    fun `throws on non-pdf content type`() {
        `when`(file.isEmpty).thenReturn(false)
        `when`(file.contentType).thenReturn("image/png")

        assertFailsWith<UnsupportedDocumentTypeException> {
            DocumentValidationService(featureFlagService).validate(file, OwnerType.USER)
        }
    }

    @Test
    fun `throws on file too large`() {
        `when`(file.isEmpty).thenReturn(false)
        `when`(file.contentType).thenReturn("application/pdf")
        `when`(file.size).thenReturn(100L * 1024 * 1024 + 1)
        `when`(featureFlagService.getTierLimits("user")).thenReturn(
            TierLimits(10, 30, 100L, 500, 100)
        )

        assertFailsWith<UnsupportedDocumentSizeException> {
            DocumentValidationService(featureFlagService).validate(file, OwnerType.USER)
        }
    }

    @Test
    fun `passes valid file`() {
        `when`(file.isEmpty).thenReturn(false)
        `when`(file.contentType).thenReturn("application/pdf")
        `when`(file.size).thenReturn(50L * 1024 * 1024)
        `when`(featureFlagService.getTierLimits("user")).thenReturn(
            TierLimits(10, 30, 100L, 500, 100)
        )

        DocumentValidationService(featureFlagService).validate(file, OwnerType.USER)
        // no exception thrown
    }
}
