package com.blaybus.backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "learning_materials")
class LearningMaterial(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    val mentor: User, // 멘토 소유
    @Column(nullable = false)
    var title: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var taskType: TaskType,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var subject: Subject,
    @Column(nullable = true) // 공부법 시리즈 -> 파일이 아닐 수도 있음
    var fileKey: String? = null,
    @Column(nullable = true)
    var originalFileName: String? = null,
    @Lob
    @Column(nullable = true)
    var content: String? = null,
) : BaseTimeEntity()
