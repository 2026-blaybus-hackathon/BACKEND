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
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
    name = "assignments",
    indexes = [
        Index(name = "idx_assignments_task_id", columnList = "task_id"),
    ],
)
class Assignment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    val task: Task,

    @Column(name = "pdf_file_name", nullable = false, length = 500)
    val pdfFileName: String,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
