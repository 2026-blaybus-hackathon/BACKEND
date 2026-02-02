package com.blaybus.backend.controller

import com.blaybus.backend.annotation.ApiErrorCodes
import com.blaybus.backend.dto.FeedbackDto
import com.blaybus.backend.exception.ErrorCode
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

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
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: FeedbackDto.CreateFeedbackDetailRequest,
        @Parameter(
            name = "taskId",
            description = "피드백을 작성할 할 일 ID",
            required = true,
            example = "1"
        )
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
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: FeedbackDto.CreateFeedbackSummaryRequest,
        @Parameter(
            name = "taskId",
            description = "피드백 요약을 작성할 할 일 ID",
            required = true,
            example = "1"
        )
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
        @AuthenticationPrincipal userId: Long,
        @Parameter(
            name = "feedbackId",
            description = "조회할 피드백 ID",
            required = true,
            example = "1"
        )
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


    @Operation(
        summary = "피드백 요약 조회",
        description = "멘티는 피드백 요약본을 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "피드백 요약 조회 성공")
    @GetMapping("/feedback/summary")
    fun getFeedbacks(
        @AuthenticationPrincipal userId: Long,
        @Parameter(
            name = "type",
            description = "피드백 필터 조건",
            required = true,
            schema = Schema(implementation = FeedbackFilterCondition::class),
            example = "KOREAN"
        )
        @RequestParam type: FeedbackFilterCondition
    ): ResponseEntity<List<FeedbackDto.FeedbackSummaryResponse>> {

        // TODO: 피드백 조회 로직 구현
        // TODO: 과목별/전날 필터링 로직 구현

        // dummy data 응답
        val summaries = when (type) {
            FeedbackFilterCondition.KOREAN -> listOf(
                FeedbackDto.FeedbackSummaryResponse(
                    id = 1L,
                    subject = FeedbackFilterCondition.KOREAN.displayName,
                    summary = "독해 문제에서 주제 파악 능력이 많이 향상되었습니다. 특히 글의 논리적 흐름을 잘 파악하고 있어요.",
                    createdAt = LocalDateTime.of(2026, 2, 1, 14, 30)
                ),
                FeedbackDto.FeedbackSummaryResponse(
                    id = 2L,
                    subject = FeedbackFilterCondition.KOREAN.displayName,
                    summary = "문법 문제에서 띄어쓰기와 맞춤법 부분을 조금 더 연습하면 좋을 것 같습니다.",
                    createdAt = LocalDateTime.of(2026, 2, 2, 15, 20)
                )
            )
            FeedbackFilterCondition.ENGLISH -> listOf(
                FeedbackDto.FeedbackSummaryResponse(
                    id = 3L,
                    subject = FeedbackFilterCondition.ENGLISH.displayName,
                    summary = "영어 단어 암기를 꾸준히 해서 어휘력이 향상되었습니다. 계속 유지하세요!",
                    createdAt = LocalDateTime.of(2026, 2, 1, 16, 10)
                ),
                FeedbackDto.FeedbackSummaryResponse(
                    id = 4L,
                    subject = FeedbackFilterCondition.ENGLISH.displayName,
                    summary = "문법 파트에서 시제 부분을 좀 더 집중적으로 공부하면 좋겠습니다.",
                    createdAt = LocalDateTime.of(2026, 2, 2, 16, 45)
                )
            )
            FeedbackFilterCondition.MATH -> listOf(
                FeedbackDto.FeedbackSummaryResponse(
                    id = 5L,
                    subject = FeedbackFilterCondition.MATH.displayName,
                    summary = "이차함수 문제 풀이가 많이 개선되었습니다. 풀이 과정이 깔끔해졌어요.",
                    createdAt = LocalDateTime.of(2026, 2, 1, 17, 30)
                ),
                FeedbackDto.FeedbackSummaryResponse(
                    id = 6L,
                    subject = FeedbackFilterCondition.MATH.displayName,
                    summary = "기하 파트에서 도형의 성질을 활용하는 연습을 더 하면 좋을 것 같습니다.",
                    createdAt = LocalDateTime.of(2026, 2, 2, 18, 0)
                )
            )
            FeedbackFilterCondition.YESTERDAY -> listOf(
                FeedbackDto.FeedbackSummaryResponse(
                    id = 2L,
                    subject = FeedbackFilterCondition.KOREAN.displayName,
                    summary = "문법 문제에서 띄어쓰기와 맞춤법 부분을 조금 더 연습하면 좋을 것 같습니다.",
                    createdAt = LocalDateTime.of(2026, 2, 2, 15, 20)
                ),
                FeedbackDto.FeedbackSummaryResponse(
                    id = 4L,
                    subject = FeedbackFilterCondition.ENGLISH.displayName,
                    summary = "문법 파트에서 시제 부분을 좀 더 집중적으로 공부하면 좋겠습니다.",
                    createdAt = LocalDateTime.of(2026, 2, 2, 16, 45)
                ),
                FeedbackDto.FeedbackSummaryResponse(
                    id = 6L,
                    subject = FeedbackFilterCondition.MATH.displayName,
                    summary = "기하 파트에서 도형의 성질을 활용하는 연습을 더 하면 좋을 것 같습니다.",
                    createdAt = LocalDateTime.of(2026, 2, 2, 18, 0)
                )
            )
        }

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(summaries)
    }
}
