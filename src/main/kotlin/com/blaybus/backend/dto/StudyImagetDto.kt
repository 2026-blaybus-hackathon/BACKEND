package com.blaybus.backend.dto

import com.blaybus.backend.entity.StudyImage
import com.blaybus.backend.entity.Task
import io.swagger.v3.oas.annotations.media.Schema

class StudyImagetDto {
    data class StudyCertificationResponse(
        @Schema(
            type = "number",
            description = "할 일 ID",
            example = "1",
        )
        val taskId: Long,
        @Schema(
            type = "string",
            description = "할 일 제목",
            example = "오늘의 수학 공부",
        )
        val title: String,
        @Schema(description = "과제 파일 리스트")
        val studyImageResponse: List<StudyImageResponse>,
        @Schema(description = "멘티의 질문 또는 코멘트")
        val menteeComment: String?,
    ) {
        constructor(task: Task, studyImageList: List<StudyImageResponse>) : this(
            taskId = task.id,
            title = task.title,
            studyImageResponse = studyImageList,
            menteeComment = task.comment,
        )
    }

    data class StudyImageResponse(
        @Schema(
            type = "number",
            description = "과제 파일 ID",
            example = "1",
        )
        val studyImageId: Long,
        @Schema(
            type = "string",
            description = "인증 사진 파일 이름",
            example = "assignment1.pdf",
        )
        val fileName: String,
        @Schema(
            type = "string",
            description = "과제 파일 URL",
            example = "https://example.com/files/assignment1.pdf",
        )
        val url: String,
    ) {
        constructor(studyImage: StudyImage, fileUrl: String) : this(
            studyImageId = studyImage.id,
            fileName = studyImage.originalFileName,
            url = fileUrl,
        )
    }
}
