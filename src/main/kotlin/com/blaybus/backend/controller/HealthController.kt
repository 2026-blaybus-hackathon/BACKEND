package com.blaybus.backend.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController {
    @GetMapping("/health")
    fun health(): String = "OK"

    @GetMapping("/ready")
    fun ready(): String = "READY"
}
