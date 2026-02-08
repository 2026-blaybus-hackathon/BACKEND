package com.blaybus.backend.util

import com.blaybus.backend.entity.Period
import mu.KotlinLogging
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

private val logger = KotlinLogging.logger {}

fun getWeekRange(date: LocalDate): Pair<LocalDate, LocalDate> {
    val startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val endOfWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    logger.debug("월요일 : {}, 일요일 : {}", startOfWeek, endOfWeek)
    return Pair(startOfWeek, endOfWeek)
}

fun getMonthRange(date: LocalDate): Pair<LocalDate, LocalDate> {
    val startOfMonth = date.with(TemporalAdjusters.firstDayOfMonth())
    val endOfMonth = date.with(TemporalAdjusters.lastDayOfMonth())
    logger.debug("월의 첫날 : {}, 월의 마지막날 : {}", startOfMonth, endOfMonth)
    return Pair(startOfMonth, endOfMonth)
}

fun getDDay(
    targetDate: LocalDate,
    currentDate: LocalDate = LocalDate.now(),
): Int =
    ChronoUnit.DAYS
        .between(currentDate, targetDate)
        .coerceAtLeast(0L)
        .toInt()

fun getFormattedPeriod(
    period: Period,
    startDate: LocalDate,
): String =
    when (period) {
        Period.WEEKLY -> {
            val weekFields = WeekFields.of(Locale.KOREA)
            val weekOfMonth = startDate.get(weekFields.weekOfMonth())
            "${startDate.monthValue}월 ${weekOfMonth}주차"
        }

        Period.MONTHLY -> {
            "${startDate.monthValue}월"
        }
    }
