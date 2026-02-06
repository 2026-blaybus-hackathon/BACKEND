package com.blaybus.backend.dto

import com.blaybus.backend.entity.Subject
import com.blaybus.backend.entity.Task
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
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
    val title: String,
    @field:Schema(description = "수정할 상세 내용")
    val content: String?,
    @field:Schema(description = "수정할 과목", allowableValues = ["KOREAN", "MATH", "ENGLISH"])
    val subject: Subject,
    @field:Schema(description = "공부 시간 (분 단위)", example = "60")
    val studyTime: Int?,
    @field:Schema(description = "완료 여부 (체크박스)", example = "true")
    val isCompleted: Boolean?,
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

data class TaskResponse(
    @Schema(description = "할 일 ID")
    val id: Long,
    @Schema(description = "할 일의 제목")
    val title: String,
    @Schema(description = "할 일의 과목", allowableValues = ["KOREAN, MATH, ENGLISH"])
    val subject: Subject,
    @Schema(description = "해당 할 일의 공부 시간 (분 단위), 안 한 경우 0")
    val studyTime: Int = 0,
    @Schema(description = "완료했는지 여부")
    val isCompleted: Boolean,
) {
    constructor(task: Task) : this(
        id = task.id,
        title = task.title,
        subject = task.subject,
        studyTime = task.studyDurationInMinutes ?: 0,
        isCompleted = task.isCompleted,
    )
}

data class TaskDetailResponse(
    @Schema(description = "할 일 ID")
    val id: Long,
    @Schema(description = "할 일의 제목")
    val title: String,
    @Schema(description = "할 일의 과목", allowableValues = ["KOREAN, MATH, ENGLISH"])
    val subject: Subject,
    @Schema(description = "해당 할 일의 공부 시간 (분 단위), 안 한 경우 0")
    val studyTime: Int = 0,
    @Schema(description = "완료했는지 여부")
    val isCompleted: Boolean,
    @Schema(description = "멘토에게 남긴 코멘트 또는 질문")
    val comment: String?,
    // TODO: 디자인 나오면 제대로 구현
) {
    constructor(task: Task) : this(
        id = task.id,
        title = task.title,
        subject = task.subject,
        studyTime = task.studyDurationInMinutes ?: 0,
        isCompleted = task.isCompleted,
        comment = task.comment,
    )
}
