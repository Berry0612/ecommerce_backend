package com.mars.ec.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    // 管理Spring Security的權限設定
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 不儲存session
                .authorizeHttpRequests(
                        authorizeHttpRequests -> authorizeHttpRequests.requestMatchers("/api/**").authenticated()

                                .anyRequest().permitAll())
                // "/api/**"需通過JWT

                .csrf(csrf -> csrf.disable())
                .addFilterBefore(new JWTAuthenticationFilter(), BasicAuthenticationFilter.class);

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
