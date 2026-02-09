package com.blaybus.backend.controller.report

import com.blaybus.backend.annotation.ApiErrorCodes
import com.blaybus.backend.dto.ReportMenteeResponse
import com.blaybus.backend.entity.Period
import com.blaybus.backend.exception.ErrorCode
import com.blaybus.backend.service.ReportService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RequestMapping("/api/v1/reports")
@RestController
class ReportController(
    private val reportService: ReportService,
) {
    @Operation(
        summary = "멘티 주/월간 리포트 조회, 존재하지 않으면 404 반환",
        description = "멘티의 주간 및 월간 리포트를 조회합니다.",
    )
    @ApiErrorCodes(ErrorCode.MENTEE_ID_REQUIRED)
    @GetMapping
    fun getMenteeReports(
        @AuthenticationPrincipal userId: Long,
        @RequestParam(required = false) menteeId: Long?,
        @RequestParam period: Period,
        @RequestParam reportDate: LocalDate,
    ): ResponseEntity<ReportMenteeResponse> {
        val response = reportService.getMenteeReport(userId, menteeId, period, reportDate)
        return response?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
    }
}
