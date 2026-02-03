package com.blaybus.backend.dto.mapper

import com.blaybus.backend.dto.MenteeResponse
import com.blaybus.backend.entity.User

fun User.toMenteesResponse() = MenteeResponse(
    menteeId = id,
    nickname = nickname,
    name = name,
    profileUrl = profileName
)