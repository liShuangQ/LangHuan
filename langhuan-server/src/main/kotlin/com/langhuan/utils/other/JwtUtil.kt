package com.langhuan.utils.other

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.DecodedJWT
import com.langhuan.common.Constant
import com.langhuan.common.TokenExpiredException
import com.langhuan.utils.date.DateTimeUtils
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtUtil {
    companion object {
        private val log = LoggerFactory.getLogger(JwtUtil::class.java)
        
        private const val SECRET = Constant.SECRET
        private const val EXPIRE = Constant.EXPIRE
        const val HEADER = Constant.HEADER
    }

    /**
     * 生成jwt token
     *
     * @param username 用户名，用于标识用户
     * @return 生成的JWT token字符串
     */
    fun generateToken(username: String): String {
        val signingKey: SecretKey = Keys.hmacShaKeyFor(SECRET.toByteArray(StandardCharsets.UTF_8))
        // 过期时间
        val tokenExpirationTime: LocalDateTime = DateTimeUtils.now().plusMinutes(EXPIRE)
        return Jwts.builder()
            .signWith(signingKey, Jwts.SIG.HS512)
            .header().add("typ", "JWT").and()
            .issuedAt(Timestamp.valueOf(DateTimeUtils.now()))
            .subject(username)
            .expiration(Timestamp.valueOf(tokenExpirationTime))
            .claims(mapOf("username" to username))
            .compact()
    }

    /**
     * 通过token获取claims信息
     *
     * @param token JWT token字符串
     * @return Claims对象，包含token中的所有信息
     * @throws TokenExpiredException 当token过期时抛出
     */
    @Throws(TokenExpiredException::class)
    fun getClaimsByToken(token: String): Claims {
        val signingKey: SecretKey = Keys.hmacShaKeyFor(SECRET.toByteArray(StandardCharsets.UTF_8))
        return try {
            Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: ExpiredJwtException) {
            throw TokenExpiredException()
        }
    }

    /**
     * 检查token是否过期
     *
     * @param expiration Token的过期时间
     * @return true：过期；false：未过期
     */
    fun isTokenExpired(expiration: Date): Boolean {
        return expiration.before(Date())
    }

    /**
     * 获得token中的自定义信息，一般是获取token的username，无需secret解密也能获得
     *
     * @param token JWT token字符串
     * @param filed 要获取的自定义信息的字段名
     * @return 字段值，如果解析出错返回null
     */
    fun getClaimFiled(token: String, filed: String): String? {
        return try {
            val jwt: DecodedJWT = JWT.decode(token)
            jwt.getClaim(filed).asString()
        } catch (e: JWTDecodeException) {
            log.error("JwtUtil getClaimFiled error: ", e)
            null
        }
    }
}

fun main(args: Array<String>) {
    val jwtUtil = JwtUtil()
    val token = jwtUtil.generateToken("admin")
    println("token = $token")

    val claims = jwtUtil.getClaimsByToken(token)
    println("claims = $claims")

    val username = jwtUtil.getClaimFiled(token, "username")
    println("username = $username")
}
