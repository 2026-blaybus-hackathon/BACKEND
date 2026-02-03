package com.blaybus.backend.repository

import com.blaybus.backend.entity.StudyImage
import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

fun StudyImageRepository.getByStudyImageId(studyImageId: Long): StudyImage =
    findById(studyImageId).orElseThrow { CustomException(ErrorCode.STUDY_IMAGE_NOT_FOUND) }

@Repository
interface StudyImageRepository : JpaRepository<StudyImage, Long>
