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

@Entity
@Table(
    name = "study_images",
    indexes = [
        Index(name = "idx_study_images_task_id", columnList = "task_id"),
    ],
)
class StudyImage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    val task: Task,
    @Column(nullable = false)
    val sequence: Int,
    @Column(nullable = false, length = 1023)
    val imageFileName: String,
    @Column(nullable = false)
    val originalFileName: String,
) : BaseTimeEntity()
