package com.blaybus.backend.exception

import com.blaybus.backend.constants.NICKNAME_MESSAGE
import com.blaybus.backend.constants.PASSWORD_MESSAGE
import org.springframework.http.HttpStatus

enum class ErrorCode(
    val httpStatus: HttpStatus,
    val code: Int,
    val errorMessage: String,
) {
    // Common API error 10000대
    BAD_REQUEST_JSON(HttpStatus.BAD_REQUEST, -10000, "잘못된 JSON 형식입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, -10002, "서버 내부 오류입니다."),
    MISSING_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, -10003, "필수 요청 파라미터가 누락되었습니다."),
    NOT_FOUND_END_POINT(HttpStatus.NOT_FOUND, -10004, "존재하지 않는 엔드포인트입니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, -10005, "유효하지 않은 입력 값입니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, -10006, "유효하지 않은 타입의 값입니다."),

    // Auth API error 11000대
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, -11000, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, -11001, "토큰이 만료되었습니다."),
    REGISTERED_ALREADY(HttpStatus.CONFLICT, -11002, "이미 가입된 이메일입니다."),
    REQUIRED_NAME(HttpStatus.BAD_REQUEST, -110003, "이름은 필수 입력값입니다."),
    CONFLICT_NICKNAME(HttpStatus.CONFLICT, -11004, "이미 존재하는 닉네임입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, -11005, "사용자를 찾을 수 없습니다."),
    UNAUTHENTICATED_ACCESS(HttpStatus.UNAUTHORIZED, -11006, "인증되지 않은 접근입니다."),
    EXPIRED_SOCIAL_SIGNUP_TOKEN(HttpStatus.UNAUTHORIZED, -11007, "소셜 회원가입 토큰이 만료되었습니다. 다시 소셜 회원가입을 요청해주세요."),
    EXPIRED_EMAIL_VERIFICATION_TOKEN(HttpStatus.BAD_REQUEST, -11008, "이메일 인증 토큰이 만료되었습니다. 다시 이메일 인증을 요청해주세요."),
    UNSUPPORTED_SOCIAL_TYPE(HttpStatus.BAD_REQUEST, -11009, "지원하지 않는 소셜 타입입니다."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, -11010, PASSWORD_MESSAGE),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, -11011, "유효하지 않은 리프레시 토큰입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, -11012, "잘못된 이메일 또는 비밀번호입니다."),
    INVALID_SOCIAL_SIGNUP_TOKEN(HttpStatus.UNAUTHORIZED, -11013, "유효하지 않은 소셜 회원가입 토큰입니다."),
    INVALID_SOCIAL_CODE(HttpStatus.BAD_REQUEST, -11014, "유효하지 않은 소셜 코드입니다."),
    INVALID_NICKNAME_FORMAT(HttpStatus.BAD_REQUEST, -11015, NICKNAME_MESSAGE),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, -11016, "접근이 금지된 사용자입니다."),
    FAILED_DELETE_USER(HttpStatus.INTERNAL_SERVER_ERROR, -11017, "사용자 탈퇴에 실패했습니다."),
    REQUIRED_PASSWORD(HttpStatus.BAD_REQUEST, -11018, "비밀번호는 필수 입력값입니다."),
    REQUIRED_EMAIL_VERIFICATION_TOKEN(HttpStatus.BAD_REQUEST, -11019, "이메일 인증 토큰은 필수 입력값입니다."),
    REQUIRED_SOCIAL_SIGNUP_TOKEN(HttpStatus.BAD_REQUEST, -11020, "소셜 회원가입 토큰은 필수 입력값입니다."),
    REQUIRED_NICKNAME(HttpStatus.BAD_REQUEST, -11021, "닉네임은 필수 입력값입니다."),
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, -11022, "유효하지 않은 이메일 형식입니다."),

    // Email-verification API error 12000대
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, -12000, "이메일 전송에 실패했습니다."),
    EMAIL_VERIFICATION_CODE_INVALID(HttpStatus.BAD_REQUEST, -12001, "유효하지 않은 이메일 인증 코드입니다."),

    // redis lock error 13000대
    REDISSON_LOCK_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, -13000, "Redis Lock 획득에 실패했습니다. 잠시 후 다시 시도해주세요."),

    // Daily Planner API error 14000대
    STUDY_TIME_MINIMUM_ERROR(HttpStatus.BAD_REQUEST, -14000, "공부 시간은 최소 0분 이상이어야 합니다."),
    STUDY_TIME_MAXIMUM_ERROR(HttpStatus.BAD_REQUEST, -14001, "공부 시간은 최대 1440분 이하여야 합니다."),
    REQUIRED_DATE(HttpStatus.BAD_REQUEST, -14002, "날짜는 비어 있을 수 없습니다."),
    REQUIRED_CONTENT(HttpStatus.BAD_REQUEST, -14003, "내용은 비어 있을 수 없습니다."),
    REQUIRED_SUBJECT(HttpStatus.BAD_REQUEST, -14004, "과목은 비어 있을 수 없습니다."),
}
