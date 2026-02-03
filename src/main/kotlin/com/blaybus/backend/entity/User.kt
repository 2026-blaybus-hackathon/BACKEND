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

@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_users_nickname", columnList = "nickname"),
        Index(name = "idx_users_mentor_id", columnList = "mentor_id"),
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
    @Column(nullable = false, length = 100, updatable = false)
    val nickname: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var role: Role,
    @Column(length = 100)
    var profileName: String? = null,
    var originFileName: String? = null,
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
}

enum class Role {
    MENTOR,
    MENTEE,
}
