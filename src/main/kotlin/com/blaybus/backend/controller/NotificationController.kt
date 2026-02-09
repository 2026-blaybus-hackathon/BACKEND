package com.blaybus.backend.controller

import com.blaybus.backend.dto.NotificationDto
import com.blaybus.backend.service.NotificationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "notification-controller API", description = "알림 관련 조회")
@RequestMapping("/api/v1/notifications/mentee")
@RestController
class NotificationController(
    private val notificationService: NotificationService,
) {
    @Operation(
        summary = "내 알림 목록 조회",
        description = "멘티는 자신에게 온 알림 목록을 최신순으로 조회할 수 있습니다.",
    )
    @GetMapping
    fun getMyNotifications(
        @AuthenticationPrincipal userId: Long,
    ): ResponseEntity<List<NotificationDto.NotificationResponse>> = ResponseEntity.ok(notificationService.getNotifications(userId))
}
