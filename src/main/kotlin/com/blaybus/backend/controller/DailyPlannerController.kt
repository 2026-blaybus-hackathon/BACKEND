package com.blaybus.backend.controller

import com.blaybus.backend.annotation.ApiErrorCodes
import com.blaybus.backend.dto.DailyPlannerCommentUpdateRequest
import com.blaybus.backend.dto.DailyPlannerTaskUpdateRequest
import com.blaybus.backend.dto.GetDailyPlannerResponse
import com.blaybus.backend.dto.UpdateDailyPlannerResponse
import com.blaybus.backend.exception.ErrorCode
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/planner")
class DailyPlannerController {
    @Operation(
        summary = "일일 플래너 조회",
        description = "인증된 사용자의 지정한 날짜에 대한 플래너를 조회합니다.",
    )
    @GetMapping
    fun getDailyPlanner(
        @AuthenticationPrincipal userId: Long,
        @RequestParam
        @Schema(description = "조회할 플래너의 날짜", required = true, example = "2026-02-02")
        date: LocalDate,
    ): ResponseEntity<GetDailyPlannerResponse> = ResponseEntity.ok().build()

    @Operation(
        summary = "일일 플래너 코멘트 수정",
        description = "인증된 사용자의 일일 플래너 코멘트를 수정합니다. 만일 해당 날짜에 대한 플래너가 없다면, 새로 생성합니다.",
    )
    @ApiErrorCodes(
        ErrorCode.REQUIRED_DATE,
    )
    @PutMapping("/comment")
    fun updateDailyPlannerComments(
        @AuthenticationPrincipal userId: Long,
        @RequestBody
        @Valid
        request: DailyPlannerCommentUpdateRequest,
    ): ResponseEntity<Unit> {
        // 만일 처음 update 요청이라면, DailyPlanner 엔티티를 생성한다.
        return ResponseEntity.ok().build()
    }

    @Operation(
        summary = "일일 플래너 공부 시간 수정",
        description = "인증된 사용자의 일일 플래너 할 일을 추가/수정/삭제합니다. 만일 해당 날짜에 대한 플래너가 없다면, 새로 생성합니다.",
    )
    @ApiErrorCodes(
        ErrorCode.STUDY_TIME_MINIMUM_ERROR,
        ErrorCode.STUDY_TIME_MAXIMUM_ERROR,
        ErrorCode.REQUIRED_DATE,
        ErrorCode.REQUIRED_CONTENT,
        ErrorCode.REQUIRED_SUBJECT,
    )
    @PutMapping("/task")
    fun updateDailyPlannerTasks(
        @AuthenticationPrincipal userId: Long,
        @Valid
        @RequestBody
        request: DailyPlannerTaskUpdateRequest,
    ): ResponseEntity<UpdateDailyPlannerResponse> {
        // 만일 처음 update 요청이라면, DailyPlanner 엔티티를 생성한다.
        return ResponseEntity.ok().build()
    }
}
