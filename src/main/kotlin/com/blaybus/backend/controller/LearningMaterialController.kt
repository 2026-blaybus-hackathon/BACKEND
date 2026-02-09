package com.blaybus.backend.controller

import com.blaybus.backend.dto.LearningMaterialRequest
import com.blaybus.backend.dto.LearningMaterialResponse
import com.blaybus.backend.entity.TaskType
import com.blaybus.backend.service.LearningMaterialService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Tag(name = "learning-material-controller", description = "멘토 학습 자료실(자료 보관) 관리")
@RequestMapping("/api/v1/materials")
@RestController
class LearningMaterialController(
    private val learningMaterialService: LearningMaterialService
) {

    @Operation(
        summary = "학습 자료 등록",
        description = "멘토가 칼럼(텍스트) 또는 솔루션(파일)을 자료실에 등록합니다. \n" +
                "- 파일 자료: `file`에 PDF 등 첨부 \n" +
                "- 텍스트 칼럼: `request.content`에 내용 입력"
    )
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createMaterial(
        @AuthenticationPrincipal userId: Long,
        @Parameter(description = "자료 정보 (JSON)", content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)])
        @Valid @RequestPart("request") request: LearningMaterialRequest,
        @Parameter(description = "첨부 파일 (선택 사항)")
        @RequestPart("file", required = false) file: MultipartFile?
    ): ResponseEntity<Void> {
        learningMaterialService.createMaterial(userId, request, file)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "학습 자료 목록 조회", description = "자료 유형(칼럼/솔루션)별로 목록을 조회합니다.")
    @GetMapping
    fun getMaterials(
        @AuthenticationPrincipal userId: Long,
        @Parameter(description = "필터링할 자료 유형 (없으면 전체 조회)")
        @RequestParam(required = false) taskType: TaskType?
    ): ResponseEntity<List<LearningMaterialResponse>> {
        val response = learningMaterialService.getMaterials(userId, taskType)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "학습 자료 삭제")
    @DeleteMapping("/{materialId}")
    fun deleteMaterial(
        @AuthenticationPrincipal userId: Long,
        @PathVariable materialId: Long
    ): ResponseEntity<Void> {
        learningMaterialService.deleteMaterial(userId, materialId)
        return ResponseEntity.noContent().build()
    }
}