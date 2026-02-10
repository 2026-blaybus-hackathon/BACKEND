package com.blaybus.backend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(
    name = "notifications",
    indexes = [
        Index(name = "idx_notifications_recipient_id", columnList = "recipient_id"),
        Index(name = "idx_notifications_created_datetime", columnList = "created_datetime"),
    ],
)
class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    val recipient: User,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    val type: NotificationType,
    @Column(nullable = true)
    val taskId: Long? = null,
    @Column(nullable = true)
    val plannerDate: LocalDate? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 10)
    val reportPeriod: Period? = null,
    @Column(nullable = true)
    val reportStartDate: LocalDate? = null,
    @Column(name = "is_read", nullable = false, columnDefinition = "NUMBER(1) DEFAULT 0")
    var isRead: Boolean = false,
) : BaseTimeEntity() {
    fun markAsRead() {
        this.isRead = true
    }
}

enum class NotificationType {
    TASK_FEEDBACK,
    PLANNER_FEEDBACK,
    REPORT,
}
