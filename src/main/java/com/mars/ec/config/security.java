package com.mars.ec.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.web.cors.CorsConfiguration; // 新增
import org.springframework.web.cors.CorsConfigurationSource; // 新增
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // 新增
import java.util.Arrays; // 新增
import java.util.List; // 新增

@Configuration
public class security {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JWTProvider jwtProvider) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable()) // 確保 CSRF 關閉，否則 POST callback 會失敗
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                    .requestMatchers("/auth/**").permitAll() // 登入註冊放行
                
                    .requestMatchers(HttpMethod.GET, "/api/product/**").permitAll()
                    //只有 ADMIN 才能新增/修改/刪除產品// 
                    .requestMatchers(HttpMethod.POST, "/api/product/**").hasRole("ADMIN") 
                    .requestMatchers(HttpMethod.PUT, "/api/product/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/product/**").hasRole("ADMIN")

                    .requestMatchers("/api/payment/callback").permitAll() // 放行綠界 Callback
                    .requestMatchers("/api/**").authenticated() // 其他 API 仍需驗證
                    .anyRequest().permitAll()
            )
            .addFilterBefore(new JWTAuthenticationFilter(jwtProvider), BasicAuthenticationFilter.class);

        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允許的前端網址 (你的 Vue 網址)
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        
        // 允許的方法 (GET, POST, PUT, DELETE...)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // 允許的 Header (例如 Authorization, Content-Type)
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        
        // 是否允許帶憑證 (Cookie/Token)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 套用到所有路徑
        return source;
    }
    
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    
}
