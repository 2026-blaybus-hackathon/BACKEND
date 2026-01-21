package com.blaybus.backend.annotation

import com.blaybus.backend.dto.ErrorResponse
import com.blaybus.backend.exception.ErrorCode
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.examples.Example
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.responses.ApiResponse
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod

@Component
class ApiErrorCodesCustomizer : OperationCustomizer {
    override fun customize(
        operation: Operation,
        handlerMethod: HandlerMethod,
    ): Operation {
        val annotation: ApiErrorCodes? =
            AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.method,
                ApiErrorCodes::class.java,
            )
        if (annotation != null) {
            val grouped: MutableMap<Int, MutableList<ErrorCode>> = HashMap()
            for (errorCode in annotation.value) {
                grouped
                    .computeIfAbsent(errorCode.httpStatus.value()) { k: Int? -> ArrayList() }
                    .add(errorCode)
            }
            // 각 status code에 대해 예시 추가
            for (entry in grouped.entries) {
                val apiResponse = ApiResponse().description("사용자 정의 오류 응답")
                val mediaType = MediaType()
                for (errorCode in entry.value) {
                    val example = Example()
                    example.summary = errorCode.name
                    example.value = ErrorResponse(errorCode.code, errorCode.errorMessage)
                    mediaType.addExamples(errorCode.name, example)
                }
                apiResponse.content =
                    Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE, mediaType)
                operation.responses.addApiResponse(entry.key.toString(), apiResponse)
            }
        }
        return operation
    }
}
