package com.blaybus.backend.controller

import com.blaybus.backend.dto.UserDto
import com.blaybus.backend.service.TestService
import com.blaybus.backend.service.user.UserService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/test")
class TestController(
    private val userService: UserService,
    private val testService: TestService,
) {
    @GetMapping("/user/list")
    fun getUserList(): List<UserDto.SimpleUserDto> = userService.findAllUser()

    @DeleteMapping("/user/{userId}")
    fun deleteUser(
        @PathVariable userId: Long,
    ) {
        userService.deleteUser(userId)
    }

    @PostMapping("upload/image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadImage(
        @RequestPart
        image: MultipartFile,
    ): String = testService.uploadImage(image)
}
