package com.blaybus.backend.dto.mapper

import com.blaybus.backend.dto.UserInfoResponse
import com.blaybus.backend.entity.User

fun User.toMenteesResponse() = UserInfoResponse(
    menteeId = id,
    nickname = nickname,
    name = name,
    profileUrl = profileName
)