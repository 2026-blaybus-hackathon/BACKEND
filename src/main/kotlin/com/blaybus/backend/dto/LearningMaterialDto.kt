package com.blaybus.backend.dto

import com.blaybus.backend.entity.LearningMaterial
import com.blaybus.backend.entity.Subject
import com.blaybus.backend.entity.TaskType
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class LearningMaterialRequest(
    @field:Schema(description = "자료 제목", example = "3월 모의고사 대비 국어 칼럼")
    val title: String,

    @field:Schema(description = "자료 유형 (칼럼, 약점 보완 솔루션)", allowableValues = ["COLUMN", "WEAKNESS_SOLUTION"])
    val taskType: TaskType,

    @field:Schema(description = "과목", allowableValues = ["KOREAN", "MATH", "ENGLISH", "HABIT_MOTIVATION", "STUDY_METHOD"])
    val subject: Subject,

    @field:Schema(description = "텍스트 내용 (파일 업로드가 아닐 경우)", nullable = true)
    val content: String? = null
)

data class LearningMaterialResponse(
    @Schema(description = "학습 자료 ID")
    val id: Long,

    @Schema(description = "제목")
    val title: String,

    @Schema(description = "자료 유형")
    val taskType: TaskType,

    @Schema(description = "과목")
    val subject: Subject,

    @Schema(description = "내용 (텍스트 칼럼인 경우)")
    val content: String?,

    @Schema(description = "원본 파일명 (파일인 경우)")
    val originalFileName: String?,

    @Schema(description = "파일 다운로드 URL (파일인 경우)")
    val fileUrl: String?,

    @Schema(description = "등록일")
    val createdDateTime: LocalDateTime
) {
    companion object {
        fun of(material: LearningMaterial, fileUrl: String?): LearningMaterialResponse {
            return LearningMaterialResponse(
                id = material.id,
                title = material.title,
                taskType = material.taskType,
                subject = material.subject,
                content = material.content,
                originalFileName = material.originalFileName,
                fileUrl = fileUrl,
                createdDateTime = material.createdDateTime
            )
        }
    }
}