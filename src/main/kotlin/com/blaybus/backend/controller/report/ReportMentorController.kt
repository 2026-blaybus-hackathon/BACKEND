package com.blaybus.backend.controller.report

import com.blaybus.backend.dto.CreateMenteeReportRequest
import com.blaybus.backend.service.ReportService
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/v1/reports/mentor")
@RestController
class ReportMentorController(
    private val reportMentorService: ReportService,
) {
    @Operation(
        summary = "멘토 주/월간 리포트 생성",
        description = "멘토의 주간 및 월간 리포트를 생성합니다.",
    )
    @PostMapping
    fun createMentorReport(
        @AuthenticationPrincipal userId: Long,
        @RequestBody
        @Valid
        request: CreateMenteeReportRequest,
    ): ResponseEntity<Unit> {
        reportMentorService.createMenteeReport(userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }
}
