package com.blaybus.backend.dto

import com.blaybus.backend.entity.Task
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

data class DailyPlannerCommentUpdateRequest(
    @Schema(description = "일일 플래너의 코멘트", required = false)
    val comment: String?,
    @Schema(description = "일일 플래너의 날짜", required = true)
    @field:NotNull(message = "날짜는 비어 있을 수 없습니다.")
    var date: LocalDate,
)

data class DailyPlannerTaskUpdateRequest(
    @Schema(description = "일일 플래너의 날짜", required = true)
    @field:NotNull(message = "날짜는 비어 있을 수 없습니다.")
    var date: LocalDate,
    @Schema(description = "추가/수정할 할 일", required = false)
    val upsertTasks: TaskUpdateRequest?,
    @Schema(description = "삭제할 할 일 ID", required = false)
    val removeTaskIds: Long?,
)

data class TaskUpdateRequest(
    @Schema(description = "할 일 ID, 새로 추가하는 할 일인 경우 null")
    val id: Long?,
    @Schema(description = "할 일의 내용", required = true)
    @field:NotBlank(message = "내용은 비어 있을 수 없습니다.")
    val content: String,
    @Schema(description = "할 일의 과목", allowableValues = ["KOREAN, MATH, ENGLISH, OTHERS"], required = true)
    @field:NotNull(message = "과목은 비어 있을 수 없습니다.")
    var subject: Subject,
    @Schema(description = "할 일의 우선순위")
    val priority: Int?,
    @field:Min(0, message = "공부 시간은 최소 0분 이상이어야 합니다.")
    @field:Max(1440, message = "공부 시간은 최대 1440분 이하여야 합니다.")
    @Schema(description = "할 일에 할당된 공부 시간 (분 단위)", example = "60", maximum = "1440", minimum = "0")
    val studyTime: Int = 0,
)

data class GetDailyPlannerResponse(
    @Schema(description = "일일 플래너 ID")
    val id: Long?,
    @Schema(description = "해당 플래너 날짜")
    val date: LocalDate,
    @Schema(description = "플래너에 대한 코멘트")
    val comment: String?,
    @Schema(description = "플래너에 포함된 할 일 목록")
    val tasks: List<TaskResponse>,
)

data class TaskResponse(
    @Schema(description = "할 일 ID")
    val id: Long,
    @Schema(description = "할 일의 내용")
    val content: String,
    @Schema(description = "할 일의 과목", allowableValues = ["KOREAN, MATH, ENGLISH, OTHERS"])
    val subject: Subject,
    @Schema(description = "할 일의 우선순위, 만일 멘토가 준 과제라면 null")
    val priority: Int?,
    @Schema(description = "할 일에 할당된 공부 시간(분 단위)")
    val studyTime: Int = 0,
)

data class UpdateDailyPlannerResponse(
    @Schema(description = "업데이트가 적용된 할 일 ID")
    val id: Long,
)

enum class Subject {
    KOREAN,
    MATH,
    ENGLISH,
    OTHERS,
}
