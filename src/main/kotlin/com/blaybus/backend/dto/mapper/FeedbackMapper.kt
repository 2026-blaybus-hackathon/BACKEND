package com.blaybus.backend.dto.mapper

import com.blaybus.backend.dto.FeedbackDto
import com.blaybus.backend.entity.Feedback
import com.blaybus.backend.entity.Task

fun Feedback.toGetFeedbackOfTaskResponse() = FeedbackDto.GetFeedbackOfTaskResponse(
    taskId = task.id,
    keepContent = keepContent,
    problemContent = problemContent,
    tryContent = tryContent,
    detail = detail
)

fun Task.toEmptyFeedbackResponse() =
    FeedbackDto.GetFeedbackOfTaskResponse(
        taskId = id,
        keepContent = null,
        problemContent = null,
        tryContent = null,
        detail = null
    )

