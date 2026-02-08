package com.blaybus.backend.controller.user

import com.blaybus.backend.dto.AchievementRateAndTotalStudyTimeResponse
import com.blaybus.backend.dto.AchievementRateResponse
import com.blaybus.backend.dto.MenteeProfileResponse
import com.blaybus.backend.entity.Period
import com.blaybus.backend.service.user.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

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

    @Operation(
        summary = "멘티 주간 과제 달성률 조회",
        description = "멘토가 특정 멘티의 과제 달성률을 조회합니다.",
    )
    @GetMapping("/mentees/{menteeId}/achievement-rate")
    fun getMenteeAchievementRate(
        @AuthenticationPrincipal userId: Long,
        @Parameter(
            name = "menteeId",
            description = "달성률을 조회할 멘티 ID",
            example = "1",
            required = true,
        )
        @PathVariable menteeId: Long,
        @RequestParam date: LocalDate,
    ): ResponseEntity<AchievementRateResponse> =
        ResponseEntity
            .status(HttpStatus.OK)
            .body(
                userService.getMenteeAchievementRate(userId, menteeId, date),
            )

    @Operation(
        summary = "주/월간 멘티 과제 달성률과 총 공부시간 조회",
        description = "멘토가 특정 멘티의 과제 달성률과 총 공부시간을 조회합니다.",
    )
    @GetMapping("/mentees/{menteeId}/stats")
    fun getMenteeStats(
        @AuthenticationPrincipal userId: Long,
        @Parameter(
            name = "menteeId",
            description = "통계를 조회할 멘티 ID",
            example = "1",
            required = true,
        )
        @PathVariable menteeId: Long,
        @RequestParam date: LocalDate,
        @RequestParam period: Period,
    ): ResponseEntity<AchievementRateAndTotalStudyTimeResponse> =
        ResponseEntity
            .status(HttpStatus.OK)
            .body(
                userService.getMenteeAchievementRateAndTotalStudyTime(userId, menteeId, date, period),
            )
}
