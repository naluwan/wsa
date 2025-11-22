package com.wsa.filter;

import com.wsa.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * JWT 認證過濾器
 * 攔截所有 HTTP 請求，從 Authorization header 中提取 JWT token 並驗證
 * 若 token 有效，則將使用者資訊設定到 Spring Security Context 中
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    /**
     * 處理每個 HTTP 請求的過濾邏輯
     * 1. 從請求中提取 JWT token
     * 2. 驗證 token 有效性
     * 3. 若有效，將使用者 ID 設定到 Security Context
     *
     * @param request HTTP 請求
     * @param response HTTP 回應
     * @param filterChain 過濾器鏈
     * @throws ServletException Servlet 例外
     * @throws IOException IO 例外
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        System.out.println("[JwtFilter] 處理請求: " + request.getMethod() + " " + requestURI);

        try {
            // 從請求中提取 JWT token
            String jwt = getJwtFromRequest(request);
            String tokenPreview = jwt != null ? jwt.substring(0, Math.min(20, jwt.length())) : "null";
            System.out.println("[JwtFilter] Token 存在: " + (jwt != null) + ", Token 前20字元: " + tokenPreview);

            // 若 token 存在且有效，則設定認證資訊
            if (StringUtils.hasText(jwt)) {
                boolean isValid = jwtService.validateToken(jwt);
                System.out.println("[JwtFilter] Token 驗證結果: " + isValid);

                if (isValid) {
                    // 從 token 中取得使用者 ID
                    UUID userId = jwtService.getUserIdFromToken(jwt);
                    System.out.println("[JwtFilter] 使用者 ID: " + userId);

                    // 建立 Spring Security 的認證物件，principal 為使用者 ID
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 將認證資訊設定到 Security Context，後續的 Controller 可透過 Authentication 取得使用者 ID
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("[JwtFilter] 認證成功，使用者 ID: " + userId);
                } else {
                    System.out.println("[JwtFilter] Token 驗證失敗");
                }
            } else {
                System.out.println("[JwtFilter] 沒有 token，跳過認證");
            }
        } catch (Exception ex) {
            // 若發生任何例外，記錄錯誤但不中斷請求處理
            System.err.println("[JwtFilter] 設定使用者認證時發生錯誤: " + ex.getMessage());
            ex.printStackTrace();
        }

        // 繼續執行下一個過濾器
        filterChain.doFilter(request, response);
    }

    /**
     * 從 HTTP 請求的 Authorization header 中提取 JWT token
     * 預期格式：Authorization: Bearer <token>
     *
     * @param request HTTP 請求
     * @return JWT token 字串，若不存在或格式錯誤則回傳 null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // 檢查 header 是否存在且以 "Bearer " 開頭
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // 移除 "Bearer " 前綴，回傳純 token 字串
            return bearerToken.substring(7);
        }
        return null;
    }
}
