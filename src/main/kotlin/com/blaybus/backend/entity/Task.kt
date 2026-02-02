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
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
    name = "tasks",
    indexes = [
        Index(name = "idx_tasks_daily_planner_id", columnList = "daily_planner_id"),
        Index(name = "idx_tasks_subject", columnList = "subject"),
    ],
)
class Task(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_planner_id", nullable = false)
    val dailyPlanner: DailyPlanner,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val subject: Subject,

    @Column(nullable = false, length = 255)
    var title: String,

    @Column(nullable = true, columnDefinition = "TEXT")
    var content: String? = null,

    @Column(nullable = true, length = 1023)
    var comment: String? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var writer: User,

    @Column(nullable = true)
    var studyTime: LocalDateTime? = null,

    @Column(nullable = false)
    var isCompleted: Boolean = false,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @OneToOne(mappedBy = "task", fetch = FetchType.LAZY)
    var feedback: Feedback? = null,

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
    val studyImages: MutableList<StudyImage> = mutableListOf(),

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
    val assignments: MutableList<Assignment> = mutableListOf(),
)

enum class Subject(val displayName: String) {
    KOREAN("국어"),
    ENGLISH("영어"),
    MATH("수학"),
}
