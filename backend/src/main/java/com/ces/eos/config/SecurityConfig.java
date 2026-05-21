package com.ces.eos.config;

import com.ces.eos.config.properties.CookieProperties;
import com.ces.eos.config.properties.CorsProperties;
import com.ces.eos.config.properties.JwtProperties;
import com.ces.eos.security.CustomAccessDeniedHandler;
import com.ces.eos.security.CustomAuthenticationEntryPoint;
import com.ces.eos.security.JwtAuthenticationFilter;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, CookieProperties.class, CorsProperties.class})
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private static final String[] PUBLIC_ENDPOINTS = {"/api/v1/auth/**", "/api/v1/health/**"};
  private static final String[] ALLOWED_CORS_METHODS = {
    "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
  };
  private static final String[] ALLOWED_CORS_HEADERS = {
    "Content-Type", "Accept", "X-Requested-With"
  };
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
  private final CustomAccessDeniedHandler customAccessDeniedHandler;
  private final CorsProperties corsProperties;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable) // CSRF disabled - will be configured later
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            exceptions ->
                exceptions
                    .authenticationEntryPoint(customAuthenticationEntryPoint)
                    .accessDeniedHandler(customAccessDeniedHandler))
        .authorizeHttpRequests(
            auth -> auth.requestMatchers(PUBLIC_ENDPOINTS).permitAll().anyRequest().authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(corsProperties.allowedOrigins());
    configuration.setAllowedMethods(Arrays.asList(ALLOWED_CORS_METHODS));
    configuration.setAllowedHeaders(Arrays.asList(ALLOWED_CORS_HEADERS));
    configuration.setAllowCredentials(true); // Required for cookies
    configuration.setMaxAge(3600L); // Cache CORS preflight response for 1 hour

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
