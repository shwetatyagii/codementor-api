package com.codementor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS Configuration
 *
 * Why needed:
 * Browser blocks requests from different origins (ports/domains) by default.
 * React runs on localhost:3000, backend on localhost:8081 — different ports = CORS error.
 * This config tells Spring Security to allow React frontend requests.
 *
 * Interview answer: "CORS is a browser security mechanism. I configured it to allow
 * requests from the React frontend origin while keeping other origins blocked."
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Allowed origins — React dev server
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173"  // Vite default port
        ));

        // Allowed HTTP methods
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        // Allow all headers including Authorization (JWT)
        config.setAllowedHeaders(List.of("*"));

        // Allow credentials (cookies, auth headers)
        config.setAllowCredentials(true);

        // How long browser caches preflight response (1 hour)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}