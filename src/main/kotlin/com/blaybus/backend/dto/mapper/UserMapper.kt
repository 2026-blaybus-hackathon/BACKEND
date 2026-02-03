package com.blaybus.backend.dto.mapper

import com.blaybus.backend.dto.MenteeProfileResponse
import com.blaybus.backend.dto.UserProfileResponse
import com.blaybus.backend.entity.User

fun User.toMenteeProfileResponse() = MenteeProfileResponse(
    menteeId = id,
    nickname = nickname,
    name = name,
    profileUrl = profileName
)
fun User.toUserProfileResponse() = UserProfileResponse(
    userId = id,
    nickname = nickname,
    name = name,
    profileUrl = profileName
)
