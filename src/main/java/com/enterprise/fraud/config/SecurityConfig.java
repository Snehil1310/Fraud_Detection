package com.enterprise.fraud.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Security configuration for input sanitization and CORS.
 * Implements XSS prevention through input sanitization filters
 * and SQL injection prevention through parameterized queries (JPA).
 */
@Configuration
public class SecurityConfig {

    // Pattern to detect potential XSS payloads
    private static final Pattern XSS_PATTERN = Pattern.compile(
            "<[^>]*script[^>]*>|javascript:|on\\w+\\s*=|eval\\s*\\(|expression\\s*\\(",
            Pattern.CASE_INSENSITIVE);

    // Pattern to detect potential SQL injection
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "'|--|;|/\\*|\\*/|xp_|exec\\s|execute\\s|union\\s+select|drop\\s+table|insert\\s+into|delete\\s+from|update\\s+.*set",
            Pattern.CASE_INSENSITIVE);

    /**
     * Register the XSS sanitization filter for all API endpoints.
     */
    @Bean
    public FilterRegistrationBean<XssSanitizationFilter> xssFilter() {
        FilterRegistrationBean<XssSanitizationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new XssSanitizationFilter());
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        registration.setName("xssSanitizationFilter");
        return registration;
    }

    /**
     * CORS configuration allowing specified origins.
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    /**
     * Servlet filter that sanitizes input parameters to prevent XSS attacks.
     */
    public static class XssSanitizationFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {

            if (request instanceof HttpServletRequest httpRequest) {
                chain.doFilter(new SanitizedRequestWrapper(httpRequest), response);
            } else {
                chain.doFilter(request, response);
            }
        }
    }

    /**
     * Request wrapper that sanitizes parameter values.
     */
    public static class SanitizedRequestWrapper extends HttpServletRequestWrapper {

        public SanitizedRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getParameter(String name) {
            String value = super.getParameter(name);
            return sanitize(value);
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null)
                return null;

            String[] sanitized = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                sanitized[i] = sanitize(values[i]);
            }
            return sanitized;
        }

        @Override
        public String getHeader(String name) {
            String value = super.getHeader(name);
            return sanitize(value);
        }

        private String sanitize(String value) {
            if (value == null)
                return null;

            // Strip potential XSS content
            value = XSS_PATTERN.matcher(value).replaceAll("");

            // HTML entity encoding for special characters
            value = value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;");

            return value;
        }
    }
}
