package com.blaybus.backend.service

import com.blaybus.backend.repository.AssignmentRepository
import org.springframework.stereotype.Service

@Service
class AssignmentService(
    private val assignmentRepository: AssignmentRepository,
) {
}
