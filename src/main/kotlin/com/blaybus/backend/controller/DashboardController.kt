package com.blaybus.backend.controller

import com.blaybus.backend.dto.MentorDashboardResponse
import com.blaybus.backend.service.TaskService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "mentor-dashboard-controller", description = "멘토의 대쉬보드 조회")
@RequestMapping("/api/v1/dashboard/mentor/")
@RestController
class DashboardController(
    val taskService: TaskService
) {
    @Operation(
        summary = "멘토 대시보드 조회",
        description = "담당 멘티 목록, 주간 학습 진행률(지난주 대비), 최근 과제 등을 조회합니다."
    )
    @GetMapping("/dashboard")
    fun getDashboard(
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<MentorDashboardResponse> { // [수정] 반환 타입 DTO 변경
        val response = taskService.getDashboardData(userId)
        return ResponseEntity.ok(response)
    }
}