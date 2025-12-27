package com.company.rms.config;

import com.company.rms.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) 
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 1. Cho phép Debug & Auth không cần token
                .requestMatchers("/api/v1/auth/**", "/api/v1/debug/**").permitAll() 
                
                // 2. Admin & Ops (Dùng hasAuthority để khớp chính xác ROLE_ trong DB)
                .requestMatchers("/api/v1/admin/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/api/v1/timesheets/lock").hasAuthority("ROLE_ADMIN")
                // Cho phép xem Master Data (GET) với bất kỳ user nào đã đăng nhập
                .requestMatchers(HttpMethod.GET, "/api/v1/master-data/**").authenticated()

                // Các thao tác ghi (POST, PUT, DELETE) chỉ dành cho ROLE_ADMIN
                .requestMatchers("/api/v1/master-data/**").hasAuthority("ROLE_ADMIN")
                // 3. Projects & Resources (Cho phép xem Dashboard)
                .requestMatchers(HttpMethod.GET, "/api/v1/projects/**").hasAnyAuthority("ROLE_PM", "ROLE_RM", "ROLE_ADMIN", "ROLE_EMP")
                .requestMatchers(HttpMethod.GET, "/api/v1/resources/**").hasAnyAuthority("ROLE_PM", "ROLE_RM", "ROLE_ADMIN", "ROLE_EMP")
                
                // 4. Các chức năng ghi (Write)
                .requestMatchers("/api/v1/resources/search").hasAnyAuthority("ROLE_PM", "ROLE_RM", "ROLE_ADMIN", "ROLE_EMP")
                .requestMatchers("/api/v1/allocations/**").hasAnyAuthority("ROLE_RM", "ROLE_ADMIN")
                
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}