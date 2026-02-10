package com.blaybus.backend.dto.mapper

import com.blaybus.backend.dto.FeedbackDetail
import com.blaybus.backend.dto.TaskDetail
import com.blaybus.backend.dto.TaskImageResponse
import com.blaybus.backend.entity.Task
import com.blaybus.backend.repository.ObjectStorageRepository

fun Task.toTaskDetail(
    objectStorageRepository: ObjectStorageRepository
): TaskDetail {
    return TaskDetail(
        taskId = id,
        subject = subject.name,
        title = title,
        time = studyDurationInMinutes,
        date = createdDateTime.toLocalDate(),
        status = isCompleted,
        menteeComment = comment,
        feedbackStatus = if (feedback != null) "COMPLETED" else "NONE",
        images = studyImages
            .sortedBy { it.sequence }
            .map {
                TaskImageResponse(
                    url = objectStorageRepository.getDownloadUrl(it.imageFileName),
                    name = it.imageFileName,
                    sequence = it.sequence
                )
            },
        feedback = feedback?.toFeedbackDetail()
            ?: FeedbackDetail(
                feedbackId = 0,
                content = null
            )
    )
}
