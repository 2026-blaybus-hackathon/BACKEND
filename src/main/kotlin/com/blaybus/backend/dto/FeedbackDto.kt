package com.blaybus.backend.dto

import com.blaybus.backend.entity.DailyPlanner
import com.blaybus.backend.entity.Feedback
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate
import java.time.LocalDateTime

class FeedbackDto {
    data class Summary(
        @field:Schema(
            description = "계속 유지하면 좋을 내용",
            example = "매일 꾸준히 학습하는 습관이 좋습니다",
            requiredMode = Schema.RequiredMode.REQUIRED,
        )
        @field:NotBlank(message = "계속 유지하면 좋을 내용 작성은 필수 입력값입니다")
        val keepContent: String,
        @field:Schema(
            description = "개선이 필요한 내용",
            example = "문제 풀이 시간을 조금 더 단축해보세요",
            requiredMode = Schema.RequiredMode.REQUIRED,
        )
        @field:NotBlank(message = "개선이 필요한 내용 작성은 필수 입력값입니다")
        val problemContent: String,
        @field:Schema(
            description = "시도해보길 권하는 내용",
            example = "타이머를 활용한 시간 제한 풀이를 시도해보세요",
            requiredMode = Schema.RequiredMode.REQUIRED,
        )
        @field:NotBlank(message = "시도해보길 권하는 내용 작성은 필수 입력값입니다")
        val tryContent: String,
    )

    data class CreateFeedbackRequest(
        @field:Schema(
            description = "KPT 형식의 피드백 요약",
            requiredMode = Schema.RequiredMode.REQUIRED,
        )
        @field:Valid
        val summary: Summary,
        @field:Schema(
            description = "피드백 상세 내용",
            example = "이번 주 학습 내용에 대한 전반적인 피드백입니다...",
            requiredMode = Schema.RequiredMode.REQUIRED,
        )
        @field:NotBlank(message = "피드백 상세 내용 작성은 필수 입력값입니다")
        val content: String,
    )

    data class CreateTotalFeedbackRequest(
        @field:Schema(
            description = "종합 피드백 내용",
            example = "오늘 학습 내용에 대한 전반적인 피드백입니다...",
            requiredMode = Schema.RequiredMode.REQUIRED,
        )
        @field:NotBlank(message = "종합 피드백 내용 작성은 필수 입력값입니다")
        val content: String,
        @Schema(
            description = "피드백을 작성할 멘티",
            requiredMode = Schema.RequiredMode.REQUIRED,
        )
        val menteeId: Long,
    )

    // Response DTOs
    data class CreateFeedbackResponse(
        @Schema(
            type = "number",
            description = "피드백 ID",
            example = "1",
        )
        val id: Long,
    )

    data class GetFeedbackOfTaskResponse(
        @Schema(
            type = "number",
            description = "해당 피드백이 있는 할 일 ID",
            example = "1",
        )
        val taskId: Long,
        @Schema(
            type = "string",
            description = "피드백 요약 중 keep 내용",
            example = "단계별로 꼼꼼히 풀이하는 습관",
        )
        val keepContent: String?,
        @Schema(
            type = "string",
            description = "피드백 요약 중 problem 내용",
            example = "독해 지문에서 개념 간 차이점 파악 미흡",
        )
        val problemContent: String?,
        @Schema(
            type = "string",
            description = "피드백 요약 중 try 내용",
            example =
                "지문을 읽을 때 비교/대조 구조 찾기\n" +
                    "주요 개념을 표로 정리하는 연습",
        )
        val tryContent: String?,
        @Schema(
            type = "string",
            description = "피드백 상세 내용",
            example =
                "지상권과 지역권 두 권리는 모두 타인의 토지를 이용하는 권리라는 상위 범주에 속하지만, 그 목적, 대상, 배타적 이용 여부, 소유권 이전 시의 효력 등에서 명확한 차이점을 보입니다.\n" +
                    "따라서 단순히 각 개념을 개별적으로 이해하는 것을 넘어 지상권과 지역권의 공통점과 차이점을 명확히 구분하여 정리하는 것이 핵심입니다.",
        )
        val detail: String?,
    )

    data class GetTotalFeedbackResponse(
        @Schema(
            type = "string",
            description = "종합 피드백 내용",
            example = "",
        )
        val totalFeedback: String?,
    )

    data class GetUnreadFeedbackCountResponse(
        @Schema(
            type = "number",
            description = "읽지 않은 피드백 수",
        )
        val unreadFeedbackCount: Long,
    )

    data class GetUnreadFeedbackResponse(
        @Schema(description = "피드백 id (과제 피드백인 경우 존재)")
        val feedbackId: Long? = null,
        @Schema(description = "할 일 id (과제 피드백인 경우 존재)")
        val taskId: Long? = null,
        @Schema(description = "할 일 제목 (과제 피드백인 경우 존재)")
        val taskTitle: String? = null,
        @Schema(description = "과목 (과제 피드백인 경우 존재)")
        val subject: String? = null,
        @Schema(description = "피드백 내용")
        val content: String?,
        @Schema(description = "플래너 날짜 (종합 피드백인 경우 존재)")
        val plannerDate: LocalDate? = null,
        @Schema(description = "피드백 생성 시간")
        val createdDateTime: LocalDate?,
        @Schema(description = "피드백 타입 (TASK: 과제 피드백, TOTAL: 종합 피드백)")
        val type: String,
        @Schema(description = "공부 시간 (분 단위, 과제 피드백은 해당 과제, 종합 피드백은 당일 총합)")
        val studyMinutes: Int?,
    ) {
        constructor(feedback: Feedback) : this(
            feedbackId = feedback.id,
            taskId = feedback.task.id,
            taskTitle = feedback.task.title,
            subject = feedback.task.subject.displayName,
            content = feedback.detail,
            createdDateTime = feedback.createdDateTime.toLocalDate(),
            type = "TASK",
            studyMinutes = feedback.task.studyDurationInMinutes,
        )

        constructor(dailyPlanner: DailyPlanner) : this(
            plannerDate = dailyPlanner.date,
            content = dailyPlanner.totalFeedback,
            createdDateTime = dailyPlanner.totalFeedbackCreatedDateTime,
            type = "TOTAL",
            studyMinutes = dailyPlanner.tasks.mapNotNull { it.studyDurationInMinutes }.sum(),
        )
    }

    data class GetFeedbackByIdResponse(
        @Schema(
            type = "object",
            description = "피드백",
        )
        val feedback: GetFeedbackOfTaskResponse,
        @Schema(description = "멘티가 과제 인증 정보")
        val studyCertificationResponse: StudyImageDto.StudyCertificationResponse,
        @Schema(description = "피드백 생성 시간")
        val createdDateTime: LocalDateTime,
    ) {
        constructor(feedback: Feedback, studyCertificationResponse: StudyImageDto.StudyCertificationResponse) : this(
            feedback =
                GetFeedbackOfTaskResponse(
                    taskId = feedback.task.id,
                    keepContent = feedback.keepContent,
                    problemContent = feedback.problemContent,
                    tryContent = feedback.tryContent,
                    detail = feedback.detail,
                ),
            studyCertificationResponse = studyCertificationResponse,
            createdDateTime = feedback.createdDateTime,
        )
    }

    data class MentorMenteeListResponse(
        @Schema(description = "멘티 ID")
        val id: Long,
        @Schema(description = "멘티 이름")
        val name: String,
        @Schema(description = "프로필 이미지 URL")
        val profileUrl: String?,
        @Schema(description = "학교명")
        val schoolName: String?,
        @Schema(description = "학년")
        val grade: String?,
        @Schema(description = "피드백 상태", allowableValues = ["PENDING", "COMPLETED"])
        val feedbackStatus: String,
    )

    data class FeedbackDetailResponse(
        @Schema(description = "멘티 ID")
        val id: Long,
        @Schema(description = "멘티 이름")
        val name: String,
        @Schema(description = "프로필 이미지 URL")
        val profileUrl: String?,
        @Schema(description = "학교명")
        val schoolName: String?,
        @Schema(description = "학년")
        val grade: String?,
        @Schema(description = "피드백 상태", allowableValues = ["PENDING", "COMPLETED"])
        val feedbackStatus: String,
    )
}
