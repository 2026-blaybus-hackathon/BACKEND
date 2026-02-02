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
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(
    name = "daily_planners",
    indexes = [
        Index(name = "idx_daily_planners_user_id", columnList = "user_id"),
        Index(name = "idx_daily_planners_date", columnList = "date"),
        Index(name = "idx_daily_planners_user_id_date", columnList = "user_id, date"),
    ],
)
class DailyPlanner(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false)
    val date: LocalDate,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "dailyPlanner", fetch = FetchType.LAZY)
    val tasks: MutableList<Task> = mutableListOf(),
)
