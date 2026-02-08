package com.blaybus.backend.dto

import com.blaybus.backend.entity.Subject
import com.blaybus.backend.entity.Task
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

data class MenteeTaskCreateRequest(
    @field:Schema(description = "할 일 제목", example = "매3비 3일차 풀기")
    @field:NotBlank(message = "제목은 필수입니다")
    val title: String,
    @field:Schema(description = "상세 내용 (선택)", example = "틀린 문제 오답노트까지 작성")
    val content: String? = null,
    @field:Schema(description = "과목", allowableValues = ["KOREAN", "MATH", "ENGLISH"])
    val subject: Subject,
    @field:Schema(description = "플래너 날짜 (어느 날짜에 등록할지)", example = "2026-02-05")
    val date: LocalDate,
)

data class MenteeTaskUpdateRequest(
    @field:Schema(description = "수정할 제목")
    val title: String? = null,
    @field:Schema(description = "수정할 상세 내용")
    val content: String? = null,
    @field:Schema(description = "수정할 과목", allowableValues = ["KOREAN", "MATH", "ENGLISH", "OTHERS"])
    val subject: Subject? = null,
    @field:Schema(description = "공부 시간 (분 단위)", example = "60")
    val studyTime: Int? = null,
    @field:Schema(description = "완료 여부 (체크박스)", example = "true")
    val isCompleted: Boolean? = null,
)

data class FileUploadResponse(
    @field:Schema(description = "업로드된 파일 ID")
    val fileId: Long,
    @field:Schema(description = "파일 접근 URL")
    val url: String,
    @field:Schema(description = "원본 파일명")
    val originalFilename: String,
)

data class CommentOnTaskRequest(
    @field:Schema(description = "멘토에게 남길 코멘트 또는 질문")
    val comment: String,
)

data class MentorTaskUpdateRequest(
    @field:Schema(description = "수정할 제목 (null이면 유지)")
    val title: String? = null,
    @field:Schema(description = "수정할 내용 (null이면 유지)")
    val content: String? = null,
    @field:Schema(description = "수정할 과목 (null이면 유지)")
    val subject: Subject? = null,
)

data class MenteeStudyTimeUpdateRequest(
    @field:Schema(description = "공부한 시간 (분 단위)", example = "60")
    @field:NotNull(message = "공부 시간은 필수입니다.")
    @field:Min(value = 0, message = "공부 시간은 0분 이상이어야 합니다.")
    var studyTime: Int,
)

data class MenteeTaskCompletionUpdateRequest(
    @field:Schema(description = "완료 여부 (true: 완료, false: 미완료)", example = "true")
    @field:NotNull(message = "완료 여부는 필수입니다.")
    var isCompleted: Boolean,
)

data class TaskResponse(
    @Schema(description = "할 일 ID")
    val id: Long,
    @Schema(description = "할일 제목")
    val title: String?,
    @Schema(description = "할 일의 내용")
    val content: String?,
    @Schema(description = "할 일의 과목", allowableValues = ["KOREAN", "MATH", "ENGLISH"])
    val subject: Subject,
    @Schema(description = "할 일에 할당된 공부 시간(분 단위)")
    val studyDurationInMinutes: Int = 0,
) {
    companion object {
        fun from(task: Task): TaskResponse =
            TaskResponse(
                id = task.id,
                title = task.title,
                content = task.content,
                subject = task.subject,
                studyDurationInMinutes = task.studyDurationInMinutes ?: 0,
            )
    }
}

data class AssignmentResponse(
    @Schema(description = "과제 ID")
    val id: Long,
    @Schema(description = "원본 파일명")
    val originalFileName: String,
    @Schema(description = "파일 접근 URL")
    val url: String,
)

data class TaskAndAssignmentResponse(
    @Schema(description = "할 일 정보")
    val task: TaskResponse,
    @Schema(description = "할 일에 대한 과제 파일 목록")
    val assignment: List<AssignmentResponse>,
    @Schema(description = "완료했는지 여부")
    val isCompleted: Boolean,
    @Schema(description = "멘토가 준 과제 여부")
    val isMentorAssigned: Boolean,
) {
    constructor(
        task: Task,
        assignment: List<AssignmentResponse>,
        isMentorAssigned: Boolean,
    ) : this(task = TaskResponse.from(task = task), assignment = assignment, task.isCompleted, !isMentorAssigned)
}

data class TaskDetailResponse(
    @Schema(description = "할 일 ID")
    val id: Long,
    @Schema(description = "할 일의 제목")
    val title: String,
    @Schema(description = "컬럼 내용")
    val content: String?,
    @Schema(description = "할 일의 과목", allowableValues = ["KOREAN", "MATH", "ENGLISH"])
    val subject: Subject,
    @Schema(description = "할 일에 할당된 공부 시간(분 단위)")
    val studyDurationInMinutes: Int = 0,
    @Schema(description = "완료했는지 여부")
    val isCompleted: Boolean,
    @Schema(description = "멘토에게 남긴 코멘트 또는 질문")
    val comment: String?,
    // TODO: 디자인 나오면 제대로 구현
) {
    constructor(task: Task) : this(
        id = task.id,
        title = task.title,
        content = task.content,
        subject = task.subject,
        studyDurationInMinutes = task.studyDurationInMinutes ?: 0,
        isCompleted = task.isCompleted,
        comment = task.comment,
    )
}

data class DailyAchievementRate(
    @Schema(description = "해당 날짜")
    val date: LocalDate,
    @Schema(description = "완료한 할 일 수")
    val completedTaskCount: Int,
    @Schema(description = "전체 할 일 수")
    val totalTaskCount: Int,
    @Schema(description = "달성률 (0~10 사이의 짝수 값)")
    val achievementRate: Int,
) {
    constructor(date: LocalDate, completedTasks: Int, totalTasks: Int) : this(
        date,
        completedTasks,
        totalTasks,
        if (totalTasks == 0 || completedTasks == 0) {
            0
        } else {
            val achievementRate = ((completedTasks.toDouble() / totalTasks) * 10).toInt()
            if (achievementRate % 2 != 0) {
                achievementRate - 1
            } else {
                achievementRate
            }
        },
    )
}

data class MentorDashboardResponse(
    @Schema(description = "대쉬보드 상단 통계 데이터")
    val stats: DashboardStatsDto,

    @Schema(description = "담당 멘티 목록")
    val mentees: List<MenteeSummaryDto>,

    @Schema(description = "최근 제출된 과제 목록 (우측 하단)")
    val recentTasks: List<RecentTaskSummaryDto>
)

data class DashboardStatsDto(
    @Schema(description = "담당 멘티 수", example = "3")
    val totalMenteeCount: Int,

    @Schema(description = "이번 주 평균 진행률 (%)", example = "80")
    val averageProgress: Int,

    @Schema(description = "지난주 대비 증감율 (%p)", example = "5")
    val progressChange: Int, // +5%면 5, -3%면 -3

    @Schema(description = "팬딩된(확인 필요한) 피드백 수", example = "1")
    val pendingFeedbackCount: Int
)

data class MenteeSummaryDto(
    @Schema(description = "멘티 ID")
    val menteeId: Long,
    @Schema(description = "이름", example = "민유진")
    val name: String,
    @Schema(description = "학교", example = "덕이고")
    val school: String,
    @Schema(description = "학년", example = "1학년")
    val grade: String,
    @Schema(description = "프로필 이미지 URL")
    val profileImageUrl: String?
)

data class RecentTaskSummaryDto(
    @Schema(description = "Task ID")
    val taskId: Long,
    @Schema(description = "과제 제목", example = "매3비 3문제")
    val title: String,
    @Schema(description = "제출한 멘티 이름", example = "홍길동")
    val menteeName: String,
    @Schema(description = "학교/학년 정보", example = "홍길동 / 고2")
    val schoolAndGrade: String,
    @Schema(description = "목표 학교", example = "한국대학교")
    val targetSchool: String,
    @Schema(description = "디데이", example = "30")
    val targetDate: LocalDate?,
    @Schema(description = "제출 날짜", example = "2026-02-05")
    val date: LocalDate,
    @Schema(description = "피드백 완료 여부")
    val isFeedbackCompleted: Boolean
)