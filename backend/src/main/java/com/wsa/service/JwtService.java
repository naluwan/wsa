package com.wsa.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 服務類別
 * 負責 JWT token 的產生、驗證與解析
 * 使用 HS256 演算法進行簽章
 */
@Service
public class JwtService {

    /** JWT 簽章密鑰（從 application.yml 讀取） */
    @Value("${jwt.secret}")
    private String secret;

    /** JWT 過期時間（毫秒，從 application.yml 讀取） */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 取得簽章密鑰
     * 將設定檔中的字串密鑰轉換為 HMAC-SHA256 簽章用的 Key 物件
     *
     * @return 簽章密鑰
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 產生 JWT token
     * token 中的 subject 欄位儲存使用者 ID
     *
     * @param userId 使用者唯一識別碼
     * @return JWT token 字串
     */
    public String generateToken(UUID userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        // 使用 HS256 演算法建立 JWT，包含使用者 ID、發行時間與過期時間
        return Jwts.builder()
                .setSubject(userId.toString())  // 在 subject 中儲存使用者 ID
                .setIssuedAt(now)               // 設定發行時間
                .setExpiration(expiryDate)       // 設定過期時間
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)  // 使用 HS256 簽章
                .compact();
    }

    /**
     * 從 JWT token 中取得使用者 ID
     * 解析 token 並從 subject 欄位中取得使用者 ID
     *
     * @param token JWT token 字串
     * @return 使用者唯一識別碼
     */
    public UUID getUserIdFromToken(String token) {
        // 解析 token 並取得 claims（聲明）
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        // 從 subject 欄位取得使用者 ID 並轉換為 UUID
        return UUID.fromString(claims.getSubject());
    }

    /**
     * 驗證 JWT token 是否有效
     * 檢查 token 的簽章、格式與過期時間
     *
     * @param token JWT token 字串
     * @return true 表示 token 有效，false 表示無效
     */
    public boolean validateToken(String token) {
        try {
            // 嘗試解析 token，若成功則表示 token 有效
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // 任何例外（簽章錯誤、過期、格式錯誤等）都視為無效 token
            return false;
        }
    }
}
