package com.blaybus.backend.constants

val ALLOWED_ORIGINS: List<String> =
    listOf(
        "http://localhost:8080",
        "http://localhost:5173",
        "http://localhost:3000",
        "https://blaybus.jayden-bin.cc",
        "https://seolstudy-99.vercel.app",
        "https://frontend-self-theta-86.vercel.app",
    )
val ALLOWED_HEADERS: List<String> =
    listOf(
        "Authorization",
        "Content-Type",
    )

val ALLOWED_HEADERS_CSV: String = ALLOWED_HEADERS.joinToString(",")

fun isAllowedOrigin(origin: String?): Boolean {
    if (origin != null) {
        for (allowedOrigin in ALLOWED_ORIGINS) {
            if (origin == allowedOrigin) return true
        }
    }
    return false
}
