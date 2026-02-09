package com.blaybus.backend.controller.user

import com.blaybus.backend.annotation.ApiErrorCodes
import com.blaybus.backend.dto.DailyAchievementRate
import com.blaybus.backend.dto.UpdateProfileRequest
import com.blaybus.backend.dto.UserProfileResponse
import com.blaybus.backend.exception.ErrorCode
import com.blaybus.backend.service.user.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
) {
    @Operation(
        summary = "주간 달성 정보 조회",
        description = "멘티는 자신의 주간 달성 정보를 조회할 수 있습니다.(유저 주간 히트맵)",
    )
    @GetMapping("/weekly-achievement-rate")
    fun getWeeklyAchievement(
        @AuthenticationPrincipal userId: Long,
        @Parameter(
            name = "date",
            description = "조회할 날짜 (해당 날짜가 속한 주의 달성 정보가 조회됩니다)",
            required = true,
        )
        @RequestParam date: LocalDate,
    ): ResponseEntity<List<DailyAchievementRate>> = ResponseEntity.ok(userService.getWeeklyAchievement(userId, date))

    @Operation(
        summary = "내 프로필 조회",
        description = "자신의 프로필을 조회할 수 있습니다.",
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
        summary = "프로필 수정",
        description = "자신의 프로필을 수정할 수 있습니다.",
    )
    @ApiErrorCodes(
        ErrorCode.REQUIRED_TARGET_SCHOOL,
    )
    @PutMapping("/profile")
    fun updateMyProfile(
        @Valid @RequestBody
        request: UpdateProfileRequest,
        @AuthenticationPrincipal userId: Long,
    ): ResponseEntity<Unit> {
        userService.updateProfile(userId, request)
        return ResponseEntity.ok().build()
    }

    @Operation(
        summary = "프로필 이미지 수정",
        description = "멘티는 자신의 프로필 이미지를 수정할 수 있습니다.",
    )
    @PatchMapping("/profile-image", consumes = [MULTIPART_FORM_DATA_VALUE])
    fun updateMyProfileImage(
        @AuthenticationPrincipal userId: Long,
        @RequestPart(required = false)
        profile: MultipartFile?,
    ): ResponseEntity<Unit> {
        userService.updateProfileImage(userId, profile)
        return ResponseEntity.ok().build()
    }
}
