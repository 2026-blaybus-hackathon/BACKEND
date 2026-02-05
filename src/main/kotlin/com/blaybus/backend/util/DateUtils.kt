package com.blaybus.backend.util

import mu.KotlinLogging
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

private val logger = KotlinLogging.logger {}

fun getWeekRange(date: LocalDate): Pair<LocalDate, LocalDate> {
    val startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val endOfWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    logger.info("월요일 : $startOfWeek, 일요일 : $endOfWeek")
    return Pair(startOfWeek, endOfWeek)
}

fun getMondayOfWeek(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
