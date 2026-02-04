package com.blaybus.backend.service

import com.blaybus.backend.repository.StudyImageRepository
import org.springframework.stereotype.Service

@Service
class StudyImageService(
    private val studyImageRepository: StudyImageRepository,
)
