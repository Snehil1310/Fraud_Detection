package com.enterprise.fraud.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting configuration using Bucket4j.
 * Applies per-IP rate limiting to the analysis endpoints to protect
 * against denial-of-service attacks.
 */
@Configuration
public class RateLimitConfig {

    private static final Logger log = LoggerFactory.getLogger(RateLimitConfig.class);

    @Value("${fraud.detection.rate-limit.requests-per-minute:20}")
    private int requestsPerMinute;

    /**
     * Register the rate limiting filter for analysis endpoints.
     */
    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RateLimitFilter(requestsPerMinute));
        registration.addUrlPatterns("/api/v1/transactions/analyze", "/api/v1/transactions/score");
        registration.setOrder(2);
        registration.setName("rateLimitFilter");
        return registration;
    }

    /**
     * Servlet filter implementing per-IP rate limiting using token bucket
     * algorithm.
     */
    public static class RateLimitFilter implements Filter {

        private static final Logger filterLog = LoggerFactory.getLogger(RateLimitFilter.class);
        private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
        private final int requestsPerMinute;

        public RateLimitFilter(int requestsPerMinute) {
            this.requestsPerMinute = requestsPerMinute;
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {

            if (request instanceof HttpServletRequest httpRequest &&
                    response instanceof HttpServletResponse httpResponse) {

                String clientIp = getClientIp(httpRequest);
                Bucket bucket = buckets.computeIfAbsent(clientIp, this::createBucket);

                if (bucket.tryConsume(1)) {
                    chain.doFilter(request, response);
                } else {
                    filterLog.warn("Rate limit exceeded for IP: {}", clientIp);
                    httpResponse.setStatus(429);
                    httpResponse.setContentType("application/json");
                    httpResponse.getWriter().write(
                            "{\"status\":429,\"error\":\"Too Many Requests\"," +
                                    "\"message\":\"Rate limit exceeded. Maximum " + requestsPerMinute +
                                    " requests per minute.\"}");
                }
            } else {
                chain.doFilter(request, response);
            }
        }

        private Bucket createBucket(String key) {
            Bandwidth limit = Bandwidth.classic(
                    requestsPerMinute,
                    Refill.greedy(requestsPerMinute, Duration.ofMinutes(1)));
            return Bucket.builder().addLimit(limit).build();
        }

        private String getClientIp(HttpServletRequest request) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
    }
}
