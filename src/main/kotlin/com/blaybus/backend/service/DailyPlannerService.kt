package com.blaybus.backend.service

import com.blaybus.backend.entity.DailyPlanner
import com.blaybus.backend.entity.User
import com.blaybus.backend.repository.DailyPlannerRepository
import com.blaybus.backend.repository.getByUserAndDate
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class DailyPlannerService(
    private val dailyPlannerRepository: DailyPlannerRepository,
) {
    fun getDailyPlannerByDate(
        user: User,
        date: LocalDate,
    ): DailyPlanner = dailyPlannerRepository.getByUserAndDate(user, date)

    fun getOrCreateDailyPlannerByDate(
        user: User,
        date: LocalDate,
    ): DailyPlanner {
        val dailyPlanner =
            try {
                dailyPlannerRepository.findByUserAndDate(user, date)
                    ?: dailyPlannerRepository.save(
                        DailyPlanner(
                            user = user,
                            date = date,
                        ),
                    )
            } catch (e: DataIntegrityViolationException) {
                dailyPlannerRepository.getByUserAndDate(user, date)
            }
        return dailyPlanner
    }

    fun getDailyPlannerOrNullByUserAndDate(
        user: User,
        date: LocalDate,
    ): DailyPlanner? =
        dailyPlannerRepository.findByUserAndDate(
            user = user,
            date = date,
        )

    fun getDailyPlannerByPeriod(
        userId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<DailyPlanner> = dailyPlannerRepository.findAllByUserIdAndDateBetweenOrderByDateAsc(userId, startDate, endDate)
}
