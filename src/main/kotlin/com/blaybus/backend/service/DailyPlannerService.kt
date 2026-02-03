package com.blaybus.backend.service

import com.blaybus.backend.repository.DailyPlannerRepository
import org.springframework.stereotype.Service

@Service
class DailyPlannerService(
    private val dailyPlannerRepository: DailyPlannerRepository,
) {
}
