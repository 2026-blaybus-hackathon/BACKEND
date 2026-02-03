package com.blaybus.backend.annotation

import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.parameters.RequestBody
import org.springframework.core.annotation.AliasFor
import java.lang.annotation.Inherited

// Swagger 에서 multipart/form-data 사용 시 OCTET_STREAM 으로 처리되는 문제 해결
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@RequestBody
annotation class SwaggerBody(
    @get:AliasFor(annotation = RequestBody::class) val description: String = "",
    @get:AliasFor(annotation = RequestBody::class) val content: Array<Content> = [],
    @get:AliasFor(annotation = RequestBody::class) val required: Boolean = false,
    @get:AliasFor(annotation = RequestBody::class) val extensions: Array<Extension> = [],
    @get:AliasFor(annotation = RequestBody::class) val ref: String = "",
)
