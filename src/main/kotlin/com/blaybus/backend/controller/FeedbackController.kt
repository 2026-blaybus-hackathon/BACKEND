package com.blaybus.backend.controller

import com.blaybus.backend.annotation.ApiErrorCodes
import com.blaybus.backend.dto.FeedbackDto
import com.blaybus.backend.exception.ErrorCode
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class FeedbackController {
    private val logger = KotlinLogging.logger {}

    // TODO: 회의 후 피드백 요약 작성, 상세 작성 두 개의 API를 하나로 만들지 결정
    @ApiErrorCodes(
        ErrorCode.FORBIDDEN_FOR_CREATE_FEEDBACK,
    )
    @Operation(summary = "피드백 상세 작성", description = "멘토는 멘티의 할 일에 피드백 상세 내용을 작성합니다.")
    @ApiResponse(responseCode = "201", description = "피드백 상세 생성 성공")
    @PostMapping("/tasks/{taskId}/feedback/detail")
    fun provideFeedback(
        @Valid @RequestBody request: FeedbackDto.CreateFeedbackDetailRequest,
        @PathVariable taskId: Long
    ): ResponseEntity<FeedbackDto.CreateFeedbackDetailResponse> {

        // TODO: 피드백을 작성할 수 있는 task인지 검증

        // 임시로 1L 응답
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                FeedbackDto.CreateFeedbackDetailResponse(1L)
            )
    }

    @ApiErrorCodes(
        ErrorCode.FORBIDDEN_FOR_CREATE_FEEDBACK,
    )
    @Operation(summary = "피드백 요약본 작성", description = "멘토는 멘티의 할 일에 피드백 요약본을 작성합니다.")
    @ApiResponse(responseCode = "201", description = "피드백 요약 생성 성공")
    @PostMapping("/tasks/{taskId}/feedback/summary")
    fun provideFeedbackSummary(
        @Valid @RequestBody request: FeedbackDto.CreateFeedbackSummaryRequest,
        @PathVariable taskId: Long
    ): ResponseEntity<FeedbackDto.CreateFeedbackSummaryResponse> {

        // TODO: 피드백을 작성할 수 있는 task인지 검증

        // 임시로 1L 응답
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                FeedbackDto.CreateFeedbackSummaryResponse(1L)
            )
    }

    @Operation(summary = "피드백 상세 조회", description = "멘티는 멘토의 피드백 상세 내용을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "피드백 상세 조회 성공")
    @GetMapping("/feedback/{feedbackId}/detail")
    fun getFeedbackDetail(
        @PathVariable feedbackId: Long
    ): ResponseEntity<FeedbackDto.GetFeedbackDetailResponse> {

        // TODO: 조회 권한 검증

        // dummy data 응답
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                FeedbackDto.GetFeedbackDetailResponse(
                    1L,
                    "오늘 중요한 실수들과 아이디어를 얻은 것 같아서 기분이 좋네요! " +
                            "틀린건 기분이 나쁠 부분이 아니라, “아 내가 이런 공통된 부분을 틀리니 " +
                            "이것만 잡으면 저걸 다 맞겠구나~”라는 생각으로 내일도 공부 화이팅입니다!"
                )
            )
    }

//     TODO: 피드백 조회 API-> (전날, 과목별 피드백 필터링 후 response)
//
//    @Operation(summary = "피드백 조회", description = "멘티는 멘토의 피드백 요약본을 조회합니다.")
//    @ApiResponse(responseCode = "200", description = "피드백 요약 조회 성공")
//    @GetMapping("/feedback/summary")
//    fun getFeedbacks(@RequestParam subject: Subject)

}
