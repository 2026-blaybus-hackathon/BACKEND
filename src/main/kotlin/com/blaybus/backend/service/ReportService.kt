package com.blaybus.backend.service

import com.blaybus.backend.dto.CreateMenteeReportRequest
import com.blaybus.backend.dto.ReportMenteeResponse
import com.blaybus.backend.dto.ReportSubjectResponse
import com.blaybus.backend.entity.Period
import com.blaybus.backend.entity.Report
import com.blaybus.backend.entity.ReportSubject
import com.blaybus.backend.entity.Role
import com.blaybus.backend.entity.Task
import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import com.blaybus.backend.repository.ReportRepository
import com.blaybus.backend.repository.TaskRepository
import com.blaybus.backend.repository.UserRepository
import com.blaybus.backend.repository.getByUserId
import com.blaybus.backend.util.getMonthRange
import com.blaybus.backend.util.getWeekRange
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import kotlin.collections.map

@Service
class ReportService(
    private val userRepository: UserRepository,
    private val reportRepository: ReportRepository,
    private val taskReportRepository: TaskRepository,
) {
    @Transactional
    fun createMenteeReport(
        userId: Long,
        request: CreateMenteeReportRequest,
    ) {
        val mentor = userRepository.getByUserId(userId)
        val mentee = userRepository.getByUserId(request.menteeId)
        mentor.validateMentee(mentee)
        val startDayAndEndDay =
            when (request.period) {
                Period.WEEKLY -> getWeekRange(request.reportDate)
                Period.MONTHLY -> getMonthRange(request.reportDate)
            }
        reportRepository
            .existsByMenteeIdAndStartDateAndEndDate(mentee.id, startDayAndEndDay.first, startDayAndEndDay.second)
            .also {
                if (it) throw CustomException(ErrorCode.CONFLICT_REPORT)
            }

        val subjectList: List<Task> =
            taskReportRepository.reportSubjectByMenteeIdAndDateBetween(
                mentee.id,
                startDayAndEndDay.first,
                startDayAndEndDay.second,
            )

        val report =
            reportRepository.save(
                Report(
                    mentee = mentee,
                    period = request.period,
                    startDate = startDayAndEndDay.first,
                    endDate = startDayAndEndDay.second,
                    overallReview = request.overallReview,
                    keepContent = request.keepContent,
                    problemContent = request.problemContent,
                    tryContent = request.tryContent,
                ),
            )

        val subjectReports =
            subjectList
                .groupBy { it.subject }
                .map { (subject, tasks) ->
                    val taskCount = tasks.size
                    val completedTaskCount = tasks.count { it.isCompleted }
                    val studyDurationInMinutes = tasks.mapNotNull { it.studyDurationInMinutes }.sum()
                    val achievementRate =
                        if (taskCount > 0) (completedTaskCount.toDouble() / taskCount * 100).toInt() else 0

                    ReportSubject(
                        report = report,
                        subject = subject,
                        totalStudyMinutes = studyDurationInMinutes,
                        achievementRate = achievementRate,
                    )
                }.toMutableList()

        val totalTasks = subjectList.size
        val totalCompletedTasks = subjectList.count { it.isCompleted }
        val totalMinutes = subjectList.mapNotNull { it.studyDurationInMinutes }.sum()
        val totalAchievementRate =
            if (totalTasks > 0) (totalCompletedTasks.toDouble() / totalTasks * 100).toInt() else 0

        report.subjectReports.clear()
        report.subjectReports.addAll(subjectReports)
        report.totalStudyMinutes = totalMinutes
        report.totalAchievementRate = totalAchievementRate

        reportRepository.save(report)
    }

    fun getMenteeReport(
        userId: Long,
        menteeId: Long,
        period: Period,
        reportDate: LocalDate,
    ): ReportMenteeResponse? {
        val user = userRepository.getByUserId(userId)
        val mentee = userRepository.getByUserId(menteeId)
        when (user.role) {
            Role.MENTOR -> {
                user.validateMentee(mentee)
            }

            Role.MENTEE -> {
                user.validateSameUser(mentee)
            }
        }

        val (startDate, endDate) =
            when (period) {
                Period.WEEKLY -> getWeekRange(reportDate)
                Period.MONTHLY -> getMonthRange(reportDate)
            }
        val report = reportRepository.findByMenteeIdAndStartDateAndEndDate(userId, startDate, endDate) ?: return null
        return ReportMenteeResponse(report)
    }

    @Transactional(readOnly = true)
    fun getMentorSubjectStats(
        userId: Long,
        menteeId: Long,
        period: Period,
        reportDate: LocalDate,
    ): List<ReportSubjectResponse> {
        val mentor = userRepository.getByUserId(userId)
        val mentee = userRepository.getByUserId(menteeId)
        mentor.validateMentee(mentee)

        val startDayAndEndDay =
            when (period) {
                Period.WEEKLY -> getWeekRange(reportDate)
                Period.MONTHLY -> getMonthRange(reportDate)
            }

        val subjectList: List<Task> =
            taskReportRepository.reportSubjectByMenteeIdAndDateBetween(
                mentee.id,
                startDayAndEndDay.first,
                startDayAndEndDay.second,
            )

        return subjectList
            .groupBy { it.subject }
            .map { (subject, tasks) ->
                val taskCount = tasks.size
                val completedTaskCount = tasks.count { it.isCompleted }
                val studyDurationInMinutes = tasks.mapNotNull { it.studyDurationInMinutes }.sum()
                val achievementRate =
                    if (taskCount > 0) (completedTaskCount.toDouble() / taskCount * 100).toInt() else 0

                ReportSubjectResponse(
                    subject = subject,
                    studyMinutes = studyDurationInMinutes,
                    achievementRate = achievementRate,
                )
            }
    }
}
