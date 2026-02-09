package com.blaybus.backend.repository

import com.blaybus.backend.entity.Notification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificationRepository : JpaRepository<Notification, Long> {
    fun findAllByRecipientIdOrderByCreatedDateTimeDesc(recipientId: Long): List<Notification>
}
