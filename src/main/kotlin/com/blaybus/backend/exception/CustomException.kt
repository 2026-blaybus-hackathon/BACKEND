package com.blaybus.backend.exception

import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

open class CustomException(
    val errorCode: ErrorCode,
    override val message: String? = errorCode.errorMessage,
) : RuntimeException(message) {
    init {
        logger.error { "CustomException occurred: Code=${errorCode.code}, ErrorMessage=${errorCode.errorMessage}, Message=$message" }
    }
}
