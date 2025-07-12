package com.langhuan.utils.other;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.langhuan.common.Constant;
import com.langhuan.utils.date.DateTimeUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

@Component
@Slf4j
public class JwtUtil {
    private static final String SECRET = Constant.SECRET;
    private static final long EXPIRE = Constant.EXPIRE;
    public static final String HEADER = Constant.HEADER;

    /**
     * 生成jwt token
     *
     * @param username 用户名，用于标识用户
     * @return 生成的JWT token字符串
     */
    public String generateToken(String username) {
        SecretKey signingKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        // 过期时间
        LocalDateTime tokenExpirationTime = DateTimeUtils.now().plusMinutes(EXPIRE);
        return Jwts.builder()
                .signWith(signingKey, Jwts.SIG.HS512)
                .header().add("typ", "JWT").and()
                .issuedAt(Timestamp.valueOf(DateTimeUtils.now()))
                .subject(username)
                .expiration(Timestamp.valueOf(tokenExpirationTime))
                .claims(Map.of("username", username))
                .compact();
    }

    /**
     * 通过token获取claims信息
     *
     * @param token JWT token字符串
     * @return Claims对象，包含token中的所有信息
     */
    public Claims getClaimsByToken(String token) {
        SecretKey signingKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 检查token是否过期
     *
     * @param expiration Token的过期时间
     * @return true：过期；false：未过期
     */
    public boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date());
    }

    /**
     * 获得token中的自定义信息，一般是获取token的username，无需secret解密也能获得
     *
     * @param token JWT token字符串
     * @param filed 要获取的自定义信息的字段名
     * @return 字段值，如果解析出错返回null
     */
    public String getClaimFiled(String token, String filed) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(filed).asString();
        } catch (JWTDecodeException e) {
            log.error("JwtUtil getClaimFiled error: ", e);
            return null;
        }
    }

    public static void main(String[] args) {
        JwtUtil jwtUtil = new JwtUtil();
        String token = jwtUtil.generateToken("admin");
        System.out.println("token = " + token);

        Claims claims = jwtUtil.getClaimsByToken(token);
        System.out.println("claims = " + claims);

        String username = jwtUtil.getClaimFiled(token, "username");
        System.out.println("username = " + username);
    }
}
