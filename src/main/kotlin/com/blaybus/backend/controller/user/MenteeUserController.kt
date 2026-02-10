package com.blaybus.backend.controller.user

import com.blaybus.backend.annotation.ApiErrorCodes
import com.blaybus.backend.dto.UpdateProfileRequest
import com.blaybus.backend.dto.UserMentorTaskStatisticsResponse
import com.blaybus.backend.dto.UserProfileResponse
import com.blaybus.backend.dto.UserTodayStudyTimeResponse
import com.blaybus.backend.exception.ErrorCode
import com.blaybus.backend.service.user.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
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

@Tag(name = "mentee-user-controller API", description = "멘티 관련 조회")
@RequestMapping("/api/v1/users/mentee")
@RestController
class MenteeUserController(
    private val userService: UserService,
) {
    @Operation(
        summary = "하루 공부량 조회",
        description = "멘티는 자신의 하루 공부량을 조회할 수 있습니다.",
    )
    @GetMapping("/study-amount")
    fun getMyDailyStudyAmount(
        @AuthenticationPrincipal userId: Long,
        @RequestParam(required = true) date: LocalDate,
    ): ResponseEntity<UserTodayStudyTimeResponse> =
        ResponseEntity
            .status(HttpStatus.OK)
            .body(userService.getDailyStudyAmount(userId, date))

    @ApiErrorCodes(
        ErrorCode.REQUIRED_NAME,
        ErrorCode.REQUIRED_TARGET_SCHOOL,
    )
    @Operation(
        summary = "멘토 과제 통계 조회",
        description = "멘티의 멘토 과제 수행 관련 통계(연속 수행 일수, 누적 공부 시간, 누적 완료 과제 수)를 조회합니다.",
    )
    @GetMapping("/statistics")
    fun getMentorTaskStatistics(
        @AuthenticationPrincipal userId: Long,
    ): ResponseEntity<UserMentorTaskStatisticsResponse> = ResponseEntity.ok(userService.getMentorTaskStatistics(userId))
}
