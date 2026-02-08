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
import jakarta.persistence.Lob
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
        Index(name = "idx_tasks_created_datetime", columnList = "created_datetime"),
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
    var subject: Subject,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var taskType: TaskType = TaskType.WEAKNESS_SOLUTION,
    @Column(nullable = false, length = 255)
    var title: String,
    @Lob
    @Column(nullable = true)
    var content: String? = null,
    @Column(nullable = true, length = 1023, name = "\"COMMENT\"")
    var comment: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var writer: User,
    @Column(nullable = true)
    var studyDurationInMinutes: Int? = null,
    @Column(name = "is_completed", nullable = false, columnDefinition = "NUMBER(1)")
    var isCompleted: Boolean = false,
    @Column(nullable = true)
    var completedTime: LocalDateTime? = null,
    @OneToOne(mappedBy = "task", fetch = FetchType.LAZY)
    var feedback: Feedback? = null,
    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
    var studyImages: MutableList<StudyImage> = mutableListOf(),
    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
    var assignments: MutableList<Assignment> = mutableListOf(),
) : BaseModifiableEntity() {
    fun updateComment(comment: String) {
        this.comment = comment
    }

    fun updateStudyDurationInMinutes(minutes: Int) {
        this.studyDurationInMinutes = minutes
    }

    fun updateCompletionStatus(isCompleted: Boolean) {
        val wasCompleted = this.isCompleted
        this.isCompleted = isCompleted
        if (!wasCompleted && isCompleted) {
            this.completedTime = LocalDateTime.now()
        } else if (wasCompleted && !isCompleted) {
            this.completedTime = null
        }
    }
}

enum class Subject(
    val displayName: String,
) {
    KOREAN("국어"),
    ENGLISH("영어"),
    MATH("수학"),
    HABIT_MOTIVATION("생활 습관&동기부여"),
    STUDY_METHOD("공부법 시리즈"),
}

enum class TaskType(val displayName: String) {
    COLUMN("칼럼"),
    WEAKNESS_SOLUTION("약점 보완 솔루션"),
}