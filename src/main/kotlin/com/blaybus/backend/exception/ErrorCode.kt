package com.blaybus.backend.exception

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
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, -11005, "사용자를 찾을 수 없습니다."),
    UNAUTHENTICATED_ACCESS(HttpStatus.UNAUTHORIZED, -11006, "인증되지 않은 접근입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, -11007, "유효하지 않은 리프레시 토큰입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, -11008, "잘못된 이메일 또는 비밀번호입니다."),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, -11009, "접근이 금지된 사용자입니다."),
    REQUIRED_TARGET_SCHOOL(HttpStatus.BAD_REQUEST, -11010, "목표 학교는 필수 입력값입니다."),
    REQUIRED_EMAIL(HttpStatus.BAD_REQUEST, -11011, "이메일은 필수 입력값입니다."),
    REQUIRED_PASSWORD(HttpStatus.BAD_REQUEST, -11012, "비밀번호는 필수 입력값입니다."),

    // Email-verification API error 12000대
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, -12000, "이메일 전송에 실패했습니다."),
    EMAIL_VERIFICATION_CODE_INVALID(HttpStatus.BAD_REQUEST, -12001, "유효하지 않은 이메일 인증 코드입니다."),

    // redis lock error 13000대
    REDISSON_LOCK_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, -13000, "Redis Lock 획득에 실패했습니다. 잠시 후 다시 시도해주세요."),

    // Daily Planner API error 14000대
    DAILY_PLANNER_NOT_FOUND(HttpStatus.NOT_FOUND, -14000, "플래너를 찾을 수 없습니다."),

    // Feedback API error 15000대
    FORBIDDEN_FOR_CREATE_FEEDBACK(HttpStatus.FORBIDDEN, -15000, "해당 할 일에 피드백을 작성할 수 없습니다."),
    FEEDBACK_NOT_FOUND(HttpStatus.FORBIDDEN, -15001, "피드백을 찾을 수 없습니다."),

    // Authorization error 16000대
    NOT_MY_MENTEE(HttpStatus.FORBIDDEN, -16000, "해당 멘티를 담당하고 있지 않습니다."),
    NOT_SAME_USER(HttpStatus.FORBIDDEN, -16001, "본인만 접근할 수 있습니다."),

    // Task API error 17000대
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, -17000, "할 일을 찾을 수 없습니다."),
    NOT_YOUR_TASK(HttpStatus.FORBIDDEN, -17001, "해당 할 일에 대한 권한이 없습니다."),

    // Assignment API error 18000대
    ASSIGNMENT_NOT_FOUND(HttpStatus.NOT_FOUND, -18000, "과제를 찾을 수 없습니다."),

    // StudyImage API error 19000대
    STUDY_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, -19000, "학습 이미지를 찾을 수 없습니다."),

    // LearningMaterial API error 20000대
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND,-20000, "학습자료를 찾을 수 없습니다."),
    NOT_YOUR_MATERIAL(HttpStatus.FORBIDDEN,-20001,"해당 학습자료에 접근 권한이 없습니다.")
}

