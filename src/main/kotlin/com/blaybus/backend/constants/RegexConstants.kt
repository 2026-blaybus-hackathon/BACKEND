package com.blaybus.backend.constants

// 비밀번호 정규식

const val PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#$%^&*]{8,20}$"

const val PASSWORD_MESSAGE = "비밀번호는 8~20자 이하, 최소 하나의 영문자와 숫자 포함, 특수문자(!@#$%^&*)만 가능합니다."

// 닉네임 정규식

const val NICKNAME_REGEX = "^[가-힣a-zA-Z0-9\\p{Script=Hiragana}\\p{Script=Katakana}\\p{Script=Han}]{2,15}$"

const val NICKNAME_MESSAGE = "닉네임은 2-15자 이하 문자(한글, 영문, 일본어, 중국어)와 숫자만 가능합니다. 공백은 허용되지 않습니다."

const val NAME_REGEX =
    "^(?=.{1,20}$)[가-힣a-zA-Z\\u3040-\\u309F\\u30A0-\\u30FF\\u4E00-\\u9FFF]+(?: [가-힣a-zA-Z\\u3040-\\u309F\\u30A0-\\u30FF\\u4E00-\\u9FFF]+)*$"

const val NAME_MESSAGE = "이름은 1-20자 이하 문자(한글, 영문, 일본어, 중국어)만 가능합니다. 중간 공백은 허용하지만 특수문자와 숫자는 허용되지 않습니다."
