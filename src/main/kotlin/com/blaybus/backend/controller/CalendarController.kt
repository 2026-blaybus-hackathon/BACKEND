package com.blaybus.backend.controller

import com.blaybus.backend.dto.CalendarPeriodResponse
import com.blaybus.backend.entity.Period
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/calendar")
class CalendarController {
    @Operation(
        summary = "주/월간 캘린더 조회",
        description = "사용자의 주/월간 캘린더를 조회합니다.",
    )
    @GetMapping
    fun getCalendar(
        @AuthenticationPrincipal userId: Long,
        @RequestParam
        @Schema(description = "조회할 플래너의 날짜", required = true, example = "2026-02-02")
        date: LocalDate,
        @RequestParam
        @Schema(description = "조회할 기간", required = true, allowableValues = ["WEEK", "MONTH"])
        period: Period,
    ): ResponseEntity<CalendarPeriodResponse> = ResponseEntity.ok().build()
}
