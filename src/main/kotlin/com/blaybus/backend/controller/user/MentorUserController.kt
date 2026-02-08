package com.blaybus.backend.controller.user

import com.blaybus.backend.dto.MenteeProfileResponse
import com.blaybus.backend.service.user.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "mentor-user-controller", description = "멘토 관련 조회")
@RequestMapping("/api/v1/users/mentor")
@RestController
class MentorUserController(
    private val userService: UserService,
) {
    @Operation(
        summary = "나의 멘티 목록 조회",
        description = "멘토가 자신에게 배정된 멘티 목록을 조회합니다.",
    )
    @GetMapping("/mentees")
    fun getMyMentees(
        @AuthenticationPrincipal userId: Long,
    ): ResponseEntity<List<MenteeProfileResponse>> =
        ResponseEntity
            .status(HttpStatus.OK)
            .body(
                userService.findAllMentees(userId),
            )

    @Operation(
        summary = "멘티 이름 검색",
        description = "멘토가 자신의 멘티들 중 특정 멘티를 이름으로 검색합니다.",
    )
    @GetMapping("/mentees/search")
    fun searchMenteesByName(
        @AuthenticationPrincipal userId: Long,
        @Parameter(
            name = "name",
            description = "검색할 멘티 이름",
            example = "홍길동",
            required = true,
        )
        @RequestParam name: String,
    ): ResponseEntity<List<MenteeProfileResponse>> =
        ResponseEntity
            .status(HttpStatus.OK)
            .body(
                userService.searchMenteesByName(userId, name),
            )
}
