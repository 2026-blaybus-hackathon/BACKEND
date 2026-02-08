package com.blaybus.backend.dto

import com.blaybus.backend.entity.Period
import com.blaybus.backend.entity.Report
import com.blaybus.backend.entity.ReportSubject
import com.blaybus.backend.entity.Subject
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

data class CreateMenteeReportRequest(
    @Schema(description = "리포트할 멘티 ID", example = "1", required = true)
    @NotNull(message = "멘티 ID는 필수입니다")
    var menteeId: Long,
    @Schema(description = "리포트 전반에 대한 리뷰", example = "이번 달 학습 태도가 매우 좋았습니다.", required = true)
    @NotBlank(message = "전반적인 리뷰는 필수입니다")
    val overallReview: String,
    @Schema(description = "기간", allowableValues = ["WEEKLY", "MONTHLY"], required = true)
    @NotNull(message = "기간은 필수입니다")
    var period: Period,
    @Schema(
        type = "string",
        description = "피드백 요약 중 keep 내용(250자)",
        example = "단계별로 꼼꼼히 풀이하는 습관",
    )
    val keepContent: String?,
    @Schema(
        type = "string",
        description = "피드백 요약 중 problem 내용",
        example = "독해 지문에서 개념 간 차이점 파악 미흡",
    )
    val problemContent: String?,
    @Schema(
        type = "string",
        description = "피드백 요약 중 try 내용",
        example =
            "지문을 읽을 때 비교/대조 구조 찾기\n" +
                "주요 개념을 표로 정리하는 연습",
    )
    val tryContent: String?,
    @Schema(description = "리포트 날짜(해당 날짜 기준으로 주간, 월간 리포트 시작일을 계산합니다)", example = "2026-02-28", required = true)
    @NotNull(message = "리포트 날짜는 필수입니다")
    var reportDate: LocalDate,
)

data class ReportMenteeResponse(
    val reportId: Long,
    val period: Period,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val overallReview: String,
    val keepContent: String?,
    val problemContent: String?,
    val tryContent: String?,
    val totalStudyHours: Int,
    val totalAchievementRate: Int,
    val subjectReports: List<ReportSubjectResponse>,
) {
    constructor(report: Report) : this(
        reportId = report.id,
        period = report.period,
        startDate = report.startDate,
        endDate = report.endDate,
        overallReview = report.overallReview,
        keepContent = report.keepContent,
        problemContent = report.problemContent,
        tryContent = report.tryContent,
        totalStudyHours = report.totalStudyHours,
        totalAchievementRate = report.totalAchievementRate,
        subjectReports = report.subjectReports.map { ReportSubjectResponse(it) },
    )
}

data class ReportSubjectResponse(
    val subject: Subject,
    val studyHours: Int,
    val achievementRate: Int,
) {
    constructor(reportSubject: ReportSubject) : this(
        subject = reportSubject.subject,
        studyHours = reportSubject.studyHours,
        achievementRate = reportSubject.achievementRate,
    )
}
