package com.blaybus.backend.service

import com.blaybus.backend.dto.NotificationDto
import com.blaybus.backend.entity.Notification
import com.blaybus.backend.entity.NotificationType
import com.blaybus.backend.entity.Period
import com.blaybus.backend.entity.User
import com.blaybus.backend.repository.NotificationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
) {
    @Transactional
    fun createNotification(
        recipient: User,
        type: NotificationType,
        taskId: Long? = null,
        plannerDate: LocalDate? = null,
        reportPeriod: Period? = null,
        reportStartDate: LocalDate? = null,
    ) {
        notificationRepository.save(
            Notification(
                recipient = recipient,
                type = type,
                taskId = taskId,
                plannerDate = plannerDate,
                reportPeriod = reportPeriod,
                reportStartDate = reportStartDate,
            ),
        )
    }

    @Transactional(readOnly = true)
    fun getNotifications(userId: Long): List<NotificationDto.NotificationResponse> =
        notificationRepository
            .findAllByRecipientIdOrderByCreatedDateTimeDesc(userId)
            .map { NotificationDto.NotificationResponse(it) }
}
