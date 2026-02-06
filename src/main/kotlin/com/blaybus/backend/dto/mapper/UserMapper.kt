package com.blaybus.backend.dto.mapper

import com.blaybus.backend.dto.MenteeProfileResponse
import com.blaybus.backend.dto.UserProfileResponse
import com.blaybus.backend.entity.User

fun User.toMenteeProfileResponse(profileUrl: String?) =
    MenteeProfileResponse(
        menteeId = id,
        name = name,
        profileUrl = profileUrl,
        // TODO: 학적, 목표, role 등 추가 정보
    )

fun User.toUserProfileResponse(profileUrl: String?) =
    UserProfileResponse(
        userId = id,
        name = name,
        profileUrl = profileUrl,
        // TODO: 학적, 목표, role 등 추가 정보
    )
