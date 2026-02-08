package com.blaybus.backend.dto

data class PagedResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalPages: Int,
    val totalElements: Long,
)

data class SliceResponse<T>(
    val content: List<T>,
    val hasNext: Boolean,
)
