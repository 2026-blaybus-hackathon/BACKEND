package com.blaybus.backend.repository

import com.blaybus.backend.entity.Assignment
import com.blaybus.backend.entity.Task
import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

fun AssignmentRepository.getByAssignmentId(assignmentId: Long): Assignment =
    findById(assignmentId).orElseThrow { CustomException(ErrorCode.ASSIGNMENT_NOT_FOUND) }

@Repository
interface AssignmentRepository : JpaRepository<Assignment, Long>{
    fun findAllByTask(task: Task): List<Assignment>
}
