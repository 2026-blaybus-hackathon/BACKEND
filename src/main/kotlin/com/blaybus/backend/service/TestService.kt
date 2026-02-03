package com.blaybus.backend.service

import com.blaybus.backend.repository.ObjectStorageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class TestService(
    private val objectStorageRepository: ObjectStorageRepository,
) {
    @Transactional
    fun uploadImage(profileImage: MultipartFile): String {
        val profileImageUrl = objectStorageRepository.upload(ObjectStorageRepository.TEST_IMAGE_PATH, profileImage)

        return objectStorageRepository.getDownloadUrl(profileImageUrl)
    }

    fun downloadImage(fileName: String): String = objectStorageRepository.getDownloadUrl(fileName)
}
