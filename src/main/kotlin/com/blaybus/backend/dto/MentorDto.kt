package com.blaybus.backend.dto

import io.swagger.v3.oas.annotations.media.Schema

data class MenteeListResponse(
    @Schema(description = "멘토 ID")
    val mentorId: Long,
    @Schema(description = "멘티 목록")
    val mentees: List<MenteeSummary>,
)

data class MenteeSummary(
    @Schema(description = "멘티 ID")
    val menteeId: Long,
    @Schema(description = "멘티 닉네임")
    val nickname: String,
    @Schema(description = "멘티 이름")
    val name: String,
    @Schema(description = "멘티 프로필 URL")
    val profileUrl: String?,
)

data class MenteeTaskFeedbackResponse(
    @Schema(description = "멘티 ID")
    val menteeId: Long,
    @Schema(description = "과제 및 피드백 목록")
    val tasks: PagedResponse<TaskDetail>,
)

data class TaskDetail(
    @Schema(description = "과제 ID")
    val taskId: Long,
    @Schema(description = "과제 제목")
    val title: String,
    @Schema(description = "과제 이미지 리스트")
    val images: List<TaskImageResponse>,
    val feedback: FeedbackDetail,
)

data class TaskImageResponse(
    @Schema(description = "과제 이미지 URL")
    val url: String,
    @Schema(description = "과제 이미지 이름")
    val name: String,
    @Schema(description = "과제 이미지 시퀀스")
    val sequence: Int,
)

data class FeedbackDetail(
    @Schema(description = "피드백 ID")
    val feedbackId: Long,
    @Schema(description = "피드백 요약")
    val summary: String,
    @Schema(description = "피드백 코멘트")
    val comment: String,
)
