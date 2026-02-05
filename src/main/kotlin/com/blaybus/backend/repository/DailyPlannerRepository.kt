package com.blaybus.backend.repository

import com.blaybus.backend.entity.DailyPlanner
import com.blaybus.backend.entity.User
import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Transactional(readOnly = true)
fun DailyPlannerRepository.getByDailyPlannerId(dailyPlannerId: Long): DailyPlanner =
    findById(dailyPlannerId).orElseThrow { CustomException(ErrorCode.DAILY_PLANNER_NOT_FOUND) }

fun DailyPlannerRepository.getByUserAndDate(
    user: User,
    date: LocalDate,
): DailyPlanner = findByUserAndDate(user, date) ?: throw CustomException(ErrorCode.DAILY_PLANNER_NOT_FOUND)

@Repository
interface DailyPlannerRepository : JpaRepository<DailyPlanner, Long> {
    fun findByUserAndDate(
        user: User,
        date: LocalDate,
    ): DailyPlanner?
}
