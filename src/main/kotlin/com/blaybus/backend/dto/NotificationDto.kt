package com.blaybus.backend.dto

import com.blaybus.backend.entity.Notification
import com.blaybus.backend.entity.NotificationType
import com.blaybus.backend.entity.Period
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

class NotificationDto {
    data class NotificationResponse(
        @Schema(description = "알림 ID")
        val id: Long,
        @Schema(description = "알림 유형 (TASK_FEEDBACK, PLANNER_FEEDBACK, REPORT)")
        val type: NotificationType,
        @Schema(description = "과제 ID (과제 피드백인 경우)")
        val taskId: Long? = null,
        @Schema(description = "플래너 날짜 (종합 피드백인 경우)")
        val plannerDate: LocalDate? = null,
        @Schema(description = "리포트 기간 (주/월간 리포트인 경우)")
        val reportPeriod: Period? = null,
        @Schema(description = "리포트 시작 날짜 (주/월간 리포트인 경우)")
        val reportStartDate: LocalDate? = null,
        @Schema(description = "읽음 여부")
        val isRead: Boolean,
        @Schema(description = "생성 일시")
        val createdDateTime: LocalDateTime,
    ) {
        constructor(notification: Notification) : this(
            id = notification.id,
            type = notification.type,
            taskId = notification.taskId,
            plannerDate = notification.plannerDate,
            reportPeriod = notification.reportPeriod,
            reportStartDate = notification.reportStartDate,
            isRead = notification.isRead,
            createdDateTime = notification.createdDateTime,
        )
    }
}
