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
    val title: String?=null,
    @field:Schema(description = "수정할 상세 내용")
    val content: String?=null,
    @field:Schema(description = "수정할 과목", allowableValues = ["KOREAN", "MATH", "ENGLISH", "OTHERS"])
    val subject: Subject?=null,
    @field:Schema(description = "공부 시간 (분 단위)", example = "60")
    val studyTime: Int?=null,
    @field:Schema(description = "완료 여부 (체크박스)", example = "true")
    val isCompleted: Boolean?=null,
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
    val studyTime: Int
)

data class MenteeTaskCompletionUpdateRequest(
    @field:Schema(description = "완료 여부 (true: 완료, false: 미완료)", example = "true")
    @field:NotNull(message = "완료 여부는 필수입니다.")
    val isCompleted: Boolean
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
        fun from(task: Task): TaskResponse {
            return TaskResponse(
                id = task.id,
                title = task.title,
                content = task.content,
                subject = task.subject,
                studyDurationInMinutes = task.studyDurationInMinutes ?: 0
            )
        }
    }
}
