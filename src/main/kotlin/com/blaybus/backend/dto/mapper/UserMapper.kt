package com.blaybus.backend.dto.mapper

import com.blaybus.backend.dto.MenteeProfileResponse
import com.blaybus.backend.dto.UserProfileResponse
import com.blaybus.backend.entity.User

fun User.toMenteeProfileResponse(profileUrl: String?) =
    MenteeProfileResponse(
        menteeId = id,
        nickname = nickname,
        name = name,
        profileUrl = profileUrl,
    )

fun User.toUserProfileResponse(profileUrl: String?) =
    UserProfileResponse(
        userId = id,
        nickname = nickname,
        name = name,
        profileUrl = profileUrl,
    )
