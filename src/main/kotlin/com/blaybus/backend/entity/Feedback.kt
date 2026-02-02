package com.blaybus.backend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
    name = "feedbacks",
    indexes = [
        Index(name = "idx_feedbacks_task_id", columnList = "task_id"),
        Index(name = "idx_feedbacks_mentor_id", columnList = "mentor_id"),
        Index(name = "idx_feedbacks_created_at", columnList = "created_at"),
    ],
)
class Feedback(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    // 1대1 관계가 맞는지 알아야 함.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false, unique = true)
    val task: Task,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    val mentor: User,

    @Column(nullable = false, columnDefinition = "TEXT")
    var summary: String,

    @Column(nullable = true, columnDefinition = "TEXT")
    var detail: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
