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
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDate

@Entity
@Table(
    name = "daily_planners",
    indexes = [
        Index(name = "idx_daily_planners_user_id", columnList = "user_id"),
        Index(name = "idx_daily_planners_date", columnList = "date"),
    ],
    uniqueConstraints =
        [
            UniqueConstraint(
                name = "uk_daily_planners_user_id_date",
                columnNames = ["user_id", "date"],
            ),
        ],
)
class DailyPlanner(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    @Column(nullable = false, name = "\"DATE\"")
    val date: LocalDate,
    @Column(nullable = true)
    var totalFeedback: String? = null, // 종합 피드백 (멘토가 멘티에게 작성)
    @Column(nullable = false, columnDefinition = "NUMBER(1) DEFAULT 0")
    var isTotalFeedbackRead: Boolean = false,
    var totalFeedbackCreatedDateTime: LocalDate? = null,
    @OneToMany(mappedBy = "dailyPlanner", fetch = FetchType.LAZY)
    val tasks: MutableList<Task> = mutableListOf(),
) : BaseModifiableEntity() {
    fun updateTotalFeedback(content: String) {
        this.totalFeedback = content
        this.isTotalFeedbackRead = false
        this.totalFeedbackCreatedDateTime = LocalDate.now()
    }

    fun readTotalFeedback() {
        this.isTotalFeedbackRead = true
    }
}
