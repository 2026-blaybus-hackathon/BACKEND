package com.blaybus.backend.entity

import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_users_mentor_id", columnList = "mentor_id"),
        Index(name = "idx_users_mentor_id_name", columnList = "mentor_id, name"),
    ],
)
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @Column(unique = true, length = 100, nullable = false, updatable = false)
    val email: String,
    @Column(nullable = false, length = 255)
    var password: String,
    @Column(nullable = false, length = 100)
    var name: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var role: Role,
    @Column(length = 100)
    var profileName: String? = null,
    var originFileName: String? = null,
    @Column(length = 100)
    var schoolName: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    var grade: Grade? = null,
    @Column(length = 100)
    var targetSchool: String? = null,
    var targetDate: LocalDate? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = true)
    var mentor: User? = null,
    @OneToMany(mappedBy = "mentor", fetch = FetchType.LAZY)
    val mentees: MutableList<User> = mutableListOf(),
) : BaseModifiableEntity() {
    fun validateMentee(user: User) {
        if (!mentees.any { it.id == user.id }) {
            throw CustomException(ErrorCode.NOT_MY_MENTEE)
        }
    }

    fun validateSameUser(user: User) {
        if (this.id != user.id) {
            throw CustomException(ErrorCode.NOT_SAME_USER)
        }
    }
}

enum class Role {
    MENTOR,
    MENTEE,
}

enum class Grade(
    val description: String,
) {
    FIRST("1학년"),
    SECOND("2학년"),
    THIRD("3학년"),
    DROPOUT("자퇴생"),
    GRADUATED("졸업생"),
}
