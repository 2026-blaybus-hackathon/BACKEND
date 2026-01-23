package com.blaybus.backend.service.auth

import com.blaybus.backend.dto.EmailVerifyResponse
import com.blaybus.backend.entity.auth.EmailVerificationToken
import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import com.blaybus.backend.repository.auth.RedisEmailVerificationTokenRepository
import com.blaybus.backend.security.JwtTokenProvider
import jakarta.mail.internet.MimeMessage
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine
import java.security.SecureRandom

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val templateEngine: SpringTemplateEngine,
    private val emailVerificationTokenRepository: RedisEmailVerificationTokenRepository,
    private val jwtTokenProvider: JwtTokenProvider,
) {
    private val logger = KotlinLogging.logger {}

    @Transactional
    fun sendVerificationEmail(email: String) {
        val code = createCode()
        val values = HashMap<String, Any>()
        values["code"] = code
        try {
            val codeFoundByEmail = emailVerificationTokenRepository.findByEmail(email)
            if (codeFoundByEmail != null) {
                emailVerificationTokenRepository.delete(codeFoundByEmail)
            }
            val message =
                createEmailMessage(
                    email,
                    values,
                )
            emailVerificationTokenRepository.save(
                EmailVerificationToken(email, code, jwtTokenProvider.emailVerificationTokenExpirationTime),
            )
            mailSender.send(message)
            // TODO: 비동기 처리 고려
            logger.info("이메일 전송 완료: {}, 인증코드: {}", email, code)
        } catch (e: Exception) {
            throw CustomException(ErrorCode.EMAIL_SEND_FAILED, e.message)
        }
    }

    @Transactional
    fun verifyEmailCode(
        email: String,
        code: String,
    ): EmailVerifyResponse {
        val codeFoundByEmail = emailVerificationTokenRepository.findByEmail(email)
        if (codeFoundByEmail == null || codeFoundByEmail.verificationCode != code) {
            throw CustomException(ErrorCode.EMAIL_VERIFICATION_CODE_INVALID)
        }
        emailVerificationTokenRepository.delete(codeFoundByEmail)
        logger.info("이메일 인증 성공: {}, 인증코드: {}", email, code)
        return EmailVerifyResponse(jwtTokenProvider.generateEmailVerificationToken(email))
    }

    private fun createCode(): String {
        val codeLength = 6
        val random = SecureRandom()
        val code = StringBuilder()
        for (i in 0..<codeLength) {
            code.append(random.nextInt(10))
        }
        return code.toString()
    }

    private fun createEmailMessage(
        email: String,
        values: HashMap<String, Any>,
    ): MimeMessage {
        val message: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")
        val context = Context()
        for (key in values.keys) {
            context.setVariable(key, values[key])
        }
        val htmlContent: String = templateEngine.process("email-verify-template", context)
        helper.setSubject("[Blaybus 해커톤] Email Verification Code")
        helper.setText(htmlContent, true)
        helper.setTo(email)
        return message
    }
}
