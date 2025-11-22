package com.wsa.config;

import com.wsa.filter.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 安全設定類別
 * 設定 JWT 認證、CORS、請求授權規則等安全相關設定
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    /**
     * 設定 Spring Security 過濾器鏈
     * 包含以下設定：
     * 1. 啟用 CORS
     * 2. 停用 CSRF（因為使用 JWT，不需要 CSRF 保護）
     * 3. 使用無狀態 session（不使用 server-side session）
     * 4. 設定請求授權規則
     * 5. 加入 JWT 過濾器
     *
     * @param http HttpSecurity 設定物件
     * @return SecurityFilterChain 安全過濾器鏈
     * @throws Exception 設定例外
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 啟用 CORS，使用自訂的 CORS 設定
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 停用 CSRF 保護（使用 JWT 進行認證，不需要 CSRF token）
                .csrf(csrf -> csrf.disable())
                // 設定 session 為無狀態（STATELESS），不建立 HTTP session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 設定請求授權規則
                .authorizeHttpRequests(auth -> auth
                        // /api/auth/** 路徑允許所有人存取（登入、註冊等）
                        .requestMatchers("/api/auth/**").permitAll()
                        // /api/** 其他路徑需要認證
                        .requestMatchers("/api/**").authenticated()
                        // 其他所有請求允許存取
                        .anyRequest().permitAll()
                )
                // 在 UsernamePasswordAuthenticationFilter 之前加入 JWT 過濾器
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 設定 CORS（跨來源資源共用）
     * 允許前端（http://localhost:3000）存取後端 API
     *
     * @return CorsConfigurationSource CORS 設定來源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允許的來源（前端網址）
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        // 允許的 HTTP 方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允許所有 headers
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // 允許傳送 cookies（httpOnly cookie 需要此設定）
        configuration.setAllowCredentials(true);

        // 將 CORS 設定套用到所有路徑
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
