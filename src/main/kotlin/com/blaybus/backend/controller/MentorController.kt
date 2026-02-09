package com.blaybus.backend.controller

import com.blaybus.backend.dto.MenteeTaskFeedbackResponse
import com.blaybus.backend.dto.MentorMyPageStatsDto
import com.blaybus.backend.service.TaskService
import com.blaybus.backend.dto.TaskWithFeedbackResponse
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/v1/mentor")
@RestController
class MentorController(
    val taskService: TaskService
) {
    @Operation(
        summary = "멘티 과제, 피드백 조회",
        description = "멘토가 특정 멘티의 과제 피드백을 조회합니다.",
    )
    @GetMapping("/mentee/{menteeId}/task-feedback")
    fun getMenteeTaskFeedback(
        @AuthenticationPrincipal userId: Long,
        @PathVariable menteeId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<List<TaskWithFeedbackResponse>> = ResponseEntity.ok().build()
    ): ResponseEntity<MenteeTaskFeedbackResponse> = ResponseEntity.ok().build()

    @Operation(summary = "멘토 마이페이지 통계 조회", description = "나의 멘티 현황(멘티 수, 평균 학습 시간, 평균 완료율)을 조회합니다.")
    @GetMapping("/mypage/stats")
    fun getMyPageStats(
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<MentorMyPageStatsDto> {
        val response = taskService.getMentorMyPageStats(userId)
        return ResponseEntity.ok(response)
    }
}
