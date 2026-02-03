package com.blaybus.backend.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate


data class MenteeTaskCreateRequest(
    @field:Schema(description = "할 일 제목", example = "매3비 3일차 풀기")
    @field:NotBlank(message = "제목은 필수입니다")
    val title: String,

    @field:Schema(description = "상세 내용 (선택)", example = "틀린 문제 오답노트까지 작성")
    val content: String? = null,

    @field:Schema(description = "과목", allowableValues = ["KOREAN", "MATH", "ENGLISH", "OTHERS"])
    val subject: Subject,

    @field:Schema(description = "플래너 날짜 (어느 날짜에 등록할지)", example = "2026-02-05")
    val date: LocalDate
)

data class MenteeTaskUpdateRequest(
    @field:Schema(description = "수정할 제목")
    val title: String?,

    @field:Schema(description = "수정할 상세 내용")
    val content: String?,

    @field:Schema(description = "수정할 과목", allowableValues = ["KOREAN", "MATH", "ENGLISH", "OTHERS"])
    val subject: Subject,

    @field:Schema(description = "공부 시간 (분 단위)", example = "60")
    val studyTime: Int?,

    @field:Schema(description = "완료 여부 (체크박스)", example = "true")
    val isCompleted: Boolean?
)

data class FileUploadResponse(
    @field:Schema(description = "업로드된 파일 ID")
    val fileId: Long,
    @field:Schema(description = "파일 접근 URL")
    val url: String,
    @field:Schema(description = "원본 파일명")
    val originalFilename: String
)