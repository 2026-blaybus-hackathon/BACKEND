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
        Index(name = "idx_users_email", columnList = "email"),
        Index(name = "idx_users_email_provider", columnList = "email, provider"),
        Index(name = "idx_users_nickname", columnList = "nickname"),
        Index(name = "idx_users_provider_providerId", columnList = "provider, providerId"),
    ],
)
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @Column(unique = true, length = 100, nullable = false, updatable = false)
    val email: String,
    @Column(nullable = true, length = 255)
    var password: String? = null,
    @Column(nullable = false, length = 100)
    var name: String,
    @Column(nullable = false, length = 100, updatable = false)
    val nickname: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    val provider: Provider = Provider.LOCAL,
    @Column(nullable = true, length = 100)
    var providerId: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var role: Role = Role.USER,
    @Column(length = 100, nullable = false)
    var contactEmail: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = true)
    var mentor: User? = null,

    @OneToMany(mappedBy = "mentor", fetch = FetchType.LAZY)
    val mentees: MutableList<User> = mutableListOf(),
) {
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

enum class Provider {
    LOCAL,
    GOOGLE,
}

enum class Role {
    USER,
    ADMIN,
}
