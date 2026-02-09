package com.blaybus.backend.service

import com.blaybus.backend.dto.LearningMaterialRequest
import com.blaybus.backend.dto.LearningMaterialResponse
import com.blaybus.backend.entity.LearningMaterial
import com.blaybus.backend.entity.TaskType
import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import com.blaybus.backend.repository.LearningMaterialRepository
import com.blaybus.backend.repository.ObjectStorageRepository
import com.blaybus.backend.repository.UserRepository
import com.blaybus.backend.repository.getByUserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional
class LearningMaterialService(
    private val learningMaterialRepository: LearningMaterialRepository,
    private val objectStorageRepository: ObjectStorageRepository,
    private val userRepository: UserRepository,
) {
    @Transactional
    fun createMaterial(
        mentorId: Long,
        request: LearningMaterialRequest,
        file: MultipartFile?,
    ) {
        val mentor = userRepository.getByUserId(mentorId)

        var fileKey: String? = null
        var originalFileName: String? = null

        if (file != null && !file.isEmpty) {
            fileKey = objectStorageRepository.upload("materials/$mentorId/", file)
            originalFileName = file.originalFilename
        }

        learningMaterialRepository.save(
            LearningMaterial(
                mentor = mentor,
                title = request.title,
                taskType = request.taskType,
                subject = request.subject,
                content = request.content,
                fileKey = fileKey,
                originalFileName = originalFileName,
            ),
        )
    }

    fun getMaterials(
        mentorId: Long,
        taskType: TaskType?,
    ): List<LearningMaterialResponse> {
        val materials = learningMaterialRepository.findAllByMentorIdAndOptionalType(mentorId, taskType)

        return materials.map { material ->
            val url = material.fileKey?.let { objectStorageRepository.getDownloadUrl(it) }

            LearningMaterialResponse.of(material, url)
        }
    }

    @Transactional
    fun deleteMaterial(
        userId: Long,
        materialId: Long,
    ) {
        val material =
            learningMaterialRepository
                .findById(materialId)
                .orElseThrow { CustomException(ErrorCode.RESOURCE_NOT_FOUND) }

        if (material.mentor.id != userId) {
            throw CustomException(ErrorCode.NOT_YOUR_MATERIAL)
        }

        material.fileKey?.let {
            objectStorageRepository.delete(it)
        }

        learningMaterialRepository.delete(material)
    }
}
