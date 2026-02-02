package com.blaybus.backend.repository

import com.blaybus.backend.config.ObjectStorageProperties
import io.awspring.cloud.s3.S3Template
import org.apache.commons.io.FilenameUtils
import org.springframework.stereotype.Repository
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream
import java.util.Locale
import java.util.UUID

@Repository
class ObjectStorageRepository(
    private val objectStorageProperties: ObjectStorageProperties,
    private val s3Template: S3Template,
) {
    fun upload(
        path: String,
        key: String,
        stream: InputStream,
    ): String {
        val objectKey = path + key
        s3Template.upload(objectStorageProperties.bucket, objectKey, stream)
        return objectKey
    }

    fun upload(
        path: String,
        file: MultipartFile,
    ): String {
        val originalFilename =
            requireNotNull(file.originalFilename) { "파일의 원본 파일명이 필요합니다." }
        val extension =
            FilenameUtils
                .getExtension(originalFilename)
                .lowercase(Locale.getDefault())
        return upload(path, generateFileName(extension), file.inputStream)
    }

    fun getDownloadUrl(key: String): String = "${objectStorageProperties.cdnUrl}/$key"

    fun delete(key: String) { // 참고로 delete는 실시간 반영되지 않음
        s3Template.deleteObject(objectStorageProperties.bucket, key)
    }

    companion object {
        const val TEST_IMAGE_PATH = "test/image/"
        const val PROFILE_IMAGE_PATH = "user/profile/"

        fun generateFileName(extension: String): String = UUID.randomUUID().toString() // 중복나지 않도록 UUID 사용
    }
}
