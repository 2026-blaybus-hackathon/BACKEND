package com.blaybus.backend.service

import com.blaybus.backend.entity.DailyPlanner
import com.blaybus.backend.entity.Role
import com.blaybus.backend.entity.User
import com.blaybus.backend.repository.DailyPlannerRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.dao.DataIntegrityViolationException
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class DailyServiceTest {
    @Mock
    lateinit var dailyPlannerRepository: DailyPlannerRepository

    @InjectMocks
    lateinit var dailyPlannerService: DailyPlannerService

    private lateinit var user: User
    private lateinit var dailyPlanner: DailyPlanner

    @BeforeEach
    fun setUp() {
        user =
            User(
                id = 1L,
                email = "mentor@test.com",
                name = "멘토",
                role = Role.MENTOR,
                password = "123",
            )

        dailyPlanner =
            DailyPlanner(
                id = 1L,
                user = user,
                date = LocalDate.now(),
            )
    }

    @Nested
    @DisplayName("GetOrCreateDailyPlannerByDate 테스트")
    inner class GetOrCreateDailyPlannerByDateTest {
        @Test
        @DisplayName("만일 데일리 플래너가 존재하지 않는다면, 새로운 데일리 플래너를 생성한다.")
        fun `데일리 플래너 생성 성공`() {
            // given
            whenever(dailyPlannerRepository.findByUserAndDate(user, dailyPlanner.date)).thenReturn(null)
            whenever(dailyPlannerRepository.save(any<DailyPlanner>())).thenReturn(dailyPlanner)

            // when
            val result = dailyPlannerService.getOrCreateDailyPlannerByDate(user, dailyPlanner.date)

            // then
            assert(result.id == dailyPlanner.id)
            verify(dailyPlannerRepository).save(any<DailyPlanner>())
        }

        @Test
        @DisplayName("만일 데일리 플래너가 이미 존재한다면, 기존 데일리 플래너를 반환한다.")
        fun `데일리 플래너 조회 성공`() {
            // given
            whenever(dailyPlannerRepository.findByUserAndDate(user, dailyPlanner.date)).thenReturn(dailyPlanner)

            // when
            val result = dailyPlannerService.getOrCreateDailyPlannerByDate(user, dailyPlanner.date)

            // then
            assert(result.id == dailyPlanner.id)
        }

        @Test
        @DisplayName("동시성 문제로 인해 데일리 플래너 생성에 실패한 경우, 기존 데일리 플래너를 조회한다.")
        fun `동시성 문제로 인한 데일리 플래너 조회 성공`() {
            // given
            whenever(dailyPlannerRepository.findByUserAndDate(user, dailyPlanner.date))
                .thenReturn(null)
                .thenReturn(dailyPlanner)
            whenever(dailyPlannerRepository.save(any<DailyPlanner>())).thenThrow(DataIntegrityViolationException(""))

            // when
            val result = dailyPlannerService.getOrCreateDailyPlannerByDate(user, dailyPlanner.date)

            // then
            assert(result.id == dailyPlanner.id)
            verify(dailyPlannerRepository).save(any<DailyPlanner>())
        }
    }
}
