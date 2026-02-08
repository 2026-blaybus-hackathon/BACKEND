package com.blaybus.backend.repository

import com.blaybus.backend.entity.Report
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ReportRepository : JpaRepository<Report, Long> {
    fun existsReportByMenteeIdAndStartDateAndEndDate(
        menteeId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Boolean

    @EntityGraph(attributePaths = ["subjectReports", "subjectReports.subject"])
    fun findByMenteeIdAndStartDateAndEndDate(
        menteeId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Report?
}
