package com.blaybus.backend.dto.mapper

import com.blaybus.backend.dto.TaskDetailsResponse
import com.blaybus.backend.dto.TaskImageResponse
import com.blaybus.backend.entity.Task
import com.blaybus.backend.repository.ObjectStorageRepository

fun Task.toTaskDetailsResponse(
    objectStorageRepository: ObjectStorageRepository
): TaskDetailsResponse {
    return TaskDetailsResponse(
        taskId = id,
        subject = subject.name,
        title = title,
        time = studyDurationInMinutes,
        date = createdDateTime.toLocalDate(),
        status = isCompleted,
        menteeComment = comment,
        feedbackStatus = feedbackStatus().name,
        images = studyImages
            .sortedBy { it.sequence }
            .map {
                TaskImageResponse(
                    url = objectStorageRepository.getDownloadUrl(it.imageFileName),
                    name = it.imageFileName,
                    sequence = it.sequence
                )
            },
        feedbackId = feedback?.id,
        feedbackContent = feedback?.detail
    )
}
