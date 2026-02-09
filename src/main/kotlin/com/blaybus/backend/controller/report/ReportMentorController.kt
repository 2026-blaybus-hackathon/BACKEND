package com.blaybus.backend.controller.report

import com.blaybus.backend.annotation.ApiErrorCodes
import com.blaybus.backend.dto.CreateMenteeReportRequest
import com.blaybus.backend.dto.ReportSubjectResponse
import com.blaybus.backend.entity.Period
import com.blaybus.backend.exception.ErrorCode
import com.blaybus.backend.service.ReportService
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RequestMapping("/api/v1/reports/mentor")
@RestController
class ReportMentorController(
    private val reportService: ReportService,
) {
    @ApiErrorCodes(
        ErrorCode.CONFLICT_REPORT,
    )
    @Operation(
        summary = "멘토 주/월간 리포트 생성",
        description = "멘토의 주간 및 월간 리포트를 생성합니다.",
    )
    @PostMapping
    fun createMenteeReport(
        @AuthenticationPrincipal userId: Long,
        @RequestBody
        @Valid
        request: CreateMenteeReportRequest,
    ): ResponseEntity<Unit> {
        reportService.createMenteeReport(userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @Operation(summary = "멘티 과목별 통계 조회", description = "멘토가 내준 과제를 과목별로 공부 시간과 완료율을 조회합니다.")
    @GetMapping("/statistics")
    fun getMentorSubjectStats(
        @AuthenticationPrincipal userId: Long,
        @RequestParam menteeId: Long,
        @RequestParam period: Period,
        @RequestParam reportDate: LocalDate,
    ): ResponseEntity<List<ReportSubjectResponse>> =
        ResponseEntity.ok(reportService.getMentorSubjectStats(userId, menteeId, period, reportDate))
}
