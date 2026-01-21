package com.blaybus.backend.annotation

import com.blaybus.backend.exception.ErrorCode

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class ApiErrorCodes(
    vararg val value: ErrorCode = [],
)
