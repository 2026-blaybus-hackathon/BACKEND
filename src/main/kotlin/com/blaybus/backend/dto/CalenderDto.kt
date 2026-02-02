package com.blaybus.backend.dto

import com.blaybus.backend.controller.Period
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class CalenderPeriodResponse(
    @Schema(description = "사용자 ID")
    val userId: Long,
    @Schema(description = "캘린더 기간", allowableValues = ["WEEK", "MONTH"])
    val period: Period,
    @Schema(description = "요청한 날짜")
    val requestedDate: LocalDate,
    @Schema(description = "시작 날짜")
    val start: LocalDate,
    @Schema(description = "종료 날짜")
    val end: LocalDate,
    @Schema(description = "날짜별 할 일 목록")
    val dates: List<CalenderDateResponse>,
)

data class CalenderDateResponse(
    @Schema(description = "날짜")
    val date: LocalDate,
    @Schema(description = "해당 날짜의 할 일 목록")
    val tasks: List<CalenderTaskResponse>,
)

data class CalenderTaskResponse(
    @Schema(description = "할 일 ID")
    val taskId: Long,
    @Schema(description = "할 일 내용")
    val content: String,
)
