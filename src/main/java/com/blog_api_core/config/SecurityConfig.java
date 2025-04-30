package com.blog_api_core.config;

import com.blog_api_core.jwt.AuthEntryPoint;
import com.blog_api_core.jwt.AuthTokenFilter;
import com.blog_api_core.jwt.CustomAccessDeniedHandler;
import com.blog_api_core.services.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final CustomUserDetailsService userDetailsService;
    private final AuthTokenFilter authTokenFilter;
    private final AuthEntryPoint authEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(CustomUserDetailsService userDetailsService, AuthTokenFilter authTokenFilter, AuthEntryPoint authEntryPoint, CustomAccessDeniedHandler accessDeniedHandler) {
        this.userDetailsService = userDetailsService;
        this.authTokenFilter = authTokenFilter;
        this.authEntryPoint = authEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults()); // plug in cors config
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(request ->
                        request
                                .requestMatchers("/v1/blog/auth/register", "/v1/blog/auth/login").permitAll()
                                .anyRequest().authenticated()
                );

        http.exceptionHandling(exceptionHandling ->
                exceptionHandling.authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
        );


        http.addFilterBefore(
            authTokenFilter, UsernamePasswordAuthenticationFilter.class
        );

        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }
}
