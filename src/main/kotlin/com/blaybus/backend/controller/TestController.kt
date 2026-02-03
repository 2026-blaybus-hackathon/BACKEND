package com.blaybus.backend.controller

import com.blaybus.backend.dto.UserDto
import com.blaybus.backend.service.TestService
import com.blaybus.backend.service.auth.AuthService
import com.blaybus.backend.service.user.UserService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v1/test")
@RestController
class TestController(
    private val userService: UserService,
    private val testService: TestService,
    private val authService: AuthService,
) {
    @Operation(summary = "모든 유저 조회")
    @GetMapping("/user/list")
    fun getUserList(): List<UserDto.SimpleUserDto> = userService.findAllUser()

    @Operation(summary = "유저 액세스 토큰 발급")
    @GetMapping("/user/{userId}")
    fun getUserAccessToken(
        @PathVariable userId: Long,
    ): String = authService.getUserAccessToken(userId)

    @Operation(summary = "유저 삭제")
    @DeleteMapping("/user/{userId}")
    fun deleteUser(
        @PathVariable userId: Long,
    ) {
        userService.deleteUser(userId)
    }

    @Operation(summary = "이미지 업로드 테스트")
    @PostMapping("upload/image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadImage(
        @RequestPart
        image: MultipartFile,
    ): String = testService.uploadImage(image)

    @Operation(summary = "이미지 다운로드 테스트")
    @GetMapping("download/image")
    fun downloadImage(fileName: String): String = testService.downloadImage(fileName)
}
