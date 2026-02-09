package com.blaybus.backend.repository

import com.blaybus.backend.entity.LearningMaterial
import com.blaybus.backend.entity.TaskType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface LearningMaterialRepository : JpaRepository<LearningMaterial, Long> {

    @Query("""
        SELECT lm 
        FROM LearningMaterial lm 
        WHERE lm.mentor.id = :mentorId 
          AND (:taskType IS NULL OR lm.taskType = :taskType)
        ORDER BY lm.createdDateTime DESC
    """)
    fun findAllByMentorIdAndOptionalType(
        mentorId: Long,
        taskType: TaskType?
    ): List<LearningMaterial>
}