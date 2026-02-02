package com.blaybus.backend.controller

import com.blaybus.backend.dto.MenteeListResponse
import com.blaybus.backend.dto.MenteeTaskFeedbackResponse
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
class MentorController {
    @Operation(
        summary = "멘티 목록 조회",
        description = "멘토가 자신의 멘티 목록을 조회합니다.",
    )
    @GetMapping("/mentee/list")
    fun getMenteeList(
        @AuthenticationPrincipal userId: Long,
    ): ResponseEntity<MenteeListResponse> = ResponseEntity.ok().build()

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
    ): ResponseEntity<MenteeTaskFeedbackResponse> = ResponseEntity.ok().build()
}
