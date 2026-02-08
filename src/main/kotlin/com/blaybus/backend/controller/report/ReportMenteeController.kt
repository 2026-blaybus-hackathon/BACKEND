package com.blaybus.backend.controller.report

import com.blaybus.backend.dto.ReportMenteeResponse
import com.blaybus.backend.entity.Period
import com.blaybus.backend.service.ReportService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RequestMapping("/api/v1/reports/mentee")
@RestController
class ReportMenteeController(
    private val reportService: ReportService,
) {
    @Operation(
        summary = "멘티 주/월간 리포트 조회",
        description = "멘티의 주간 및 월간 리포트를 조회합니다. 존재하지 않는 경우 null을 반환합니다.",
    )
    @GetMapping
    fun getMenteeReports(
        @AuthenticationPrincipal userId: Long,
        @RequestParam period: Period,
        @RequestParam reportDate: LocalDate,
    ): ResponseEntity<ReportMenteeResponse>? {
        val response = reportService.getMenteeReport(userId, period, reportDate)
        return ResponseEntity.ok(response)
    }
}
