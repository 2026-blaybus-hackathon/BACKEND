package com.blaybus.backend.controller.user

import com.blaybus.backend.dto.UserProfileResponse
import com.blaybus.backend.dto.UserTodayStudyTimeDto
import com.blaybus.backend.service.user.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "mentee-user-controller API", description = "멘티 관련 조회")
@RequestMapping("/api/v1/users/mentee")
@RestController
class MenteeUserController(
    private val userService: UserService,
) {
    // TODO : 멘토, 멘티 공통 프로필 조회 API로 변경될 수도 있음
    @Operation(
        summary = "내 프로필 조회",
        description = "멘티는 자신의 프로필을 조회할 수 있습니다.",
    )
    @GetMapping("/profile")
    fun getMyProfile(
        @AuthenticationPrincipal userId: Long,
    ): ResponseEntity<UserProfileResponse> =
        ResponseEntity
            .status(HttpStatus.OK)
            .body(
                userService.findMyProfile(userId),
            )

    @Operation(
        summary = "하루 공부량 조회",
        description = "멘티는 자신의 하루 공부량을 조회할 수 있습니다.",
    )
    @GetMapping("/daily-study-amount")
    fun getMyDailyStudyAmount(
        @AuthenticationPrincipal userId: Long,
    ): ResponseEntity<UserTodayStudyTimeDto> =
        ResponseEntity
            .status(HttpStatus.OK)
            .body(userService.getDailyStudyAmount(userId))
}
