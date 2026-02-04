package com.blaybus.backend.controller

import com.blaybus.backend.dto.MenteeListResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "User API", description = "사용자 정보 및 멘티/멘토 관계 조회")
@RequestMapping("/api/v1/users") // URL 변경: users 자원 사용
@RestController
class UserController {
    @Operation(
        summary = "나의 멘티 목록 조회",
        description = "멘토가 자신에게 배정된 멘티 목록을 조회합니다.",
    )
    @GetMapping("/mentees") // URL: /api/v1/users/mentees
    fun getMyMentees(
        @AuthenticationPrincipal userId: Long,
    ): ResponseEntity<MenteeListResponse> = ResponseEntity.ok().build()
}
