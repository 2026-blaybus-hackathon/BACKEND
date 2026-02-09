package com.blaybus.backend.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDate

@Entity
@Table(
    name = "report",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uc_report_mentee_id_start_date_end_date",
            columnNames = ["mentee_id", "start_date", "end_date"],
        ),
    ],
)
class Report(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    val period: Period, // WEEKLY, MONTHLY
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentee_id", nullable = false)
    val mentee: User,
    @Column(nullable = false, updatable = false, name = "start_date")
    val startDate: LocalDate,
    @Column(nullable = false, updatable = false, name = "end_date")
    val endDate: LocalDate,
    @Column(columnDefinition = "CLOB", nullable = false)
    var overallReview: String,
    @Column(nullable = true)
    var keepContent: String?,
    @Column(nullable = true)
    var problemContent: String?,
    @Column(nullable = true)
    var tryContent: String?,
    @Column(nullable = false)
    var totalStudyMinutes: Int = 0,
    @Column(nullable = false)
    var totalAchievementRate: Int = 0,
    @OneToMany(mappedBy = "report", cascade = [CascadeType.ALL], orphanRemoval = true)
    var subjectReports: MutableList<ReportSubject> = mutableListOf(),
)
