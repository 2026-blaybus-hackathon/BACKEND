package com.blaybus.backend.entity

import jakarta.persistence.Persistence
import org.junit.jupiter.api.Test

class EntityDdlTest {
    @Test
    fun `엔티티 DDL 생성 및 출력`() {
        val properties =
            mapOf(
                "jakarta.persistence.jdbc.driver" to "org.h2.Driver",
                "jakarta.persistence.jdbc.url" to "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
                "jakarta.persistence.jdbc.user" to "sa",
                "jakarta.persistence.jdbc.password" to "",
                "hibernate.dialect" to "org.hibernate.dialect.H2Dialect",
                "hibernate.hbm2ddl.auto" to "create",
                "hibernate.show_sql" to "true",
                "hibernate.format_sql" to "true",
            )

        println("========== DDL 생성 시작 ==========")

        // EntityManagerFactory 생성 시 DDL이 자동으로 실행됨
        val emf = Persistence.createEntityManagerFactory("test-pu", properties)
        val em = emf.createEntityManager()

        // 테이블 목록 조회
        val tables = em.createNativeQuery("SHOW TABLES").resultList
        println("\n========== 생성된 테이블 목록 ==========")
        tables.forEach { println("  - $it") }
        println("========================================\n")

        em.close()
        emf.close()

        println("✅ DDL 생성 완료!")
    }
}
