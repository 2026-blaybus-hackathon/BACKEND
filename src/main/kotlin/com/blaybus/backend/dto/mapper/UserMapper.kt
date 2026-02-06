package com.blaybus.backend.dto.mapper

import com.blaybus.backend.dto.MenteeProfileResponse
import com.blaybus.backend.dto.UserProfileResponse
import com.blaybus.backend.entity.User

fun User.toMenteeProfileResponse(profileUrl: String?) = MenteeProfileResponse(this, profileUrl)

fun User.toUserProfileResponse(profileUrl: String?) = UserProfileResponse(this, profileUrl)
