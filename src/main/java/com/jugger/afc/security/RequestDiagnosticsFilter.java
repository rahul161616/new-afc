package com.jugger.afc.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
public class RequestDiagnosticsFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        boolean shouldLog = shouldLog(request);
        long startedAt = System.currentTimeMillis();

        if (shouldLog) {
            log.info(
                    "Incoming request method={} path={} origin={} requestMethod={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getHeader("Origin"),
                    request.getHeader("Access-Control-Request-Method")
            );
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            if (shouldLog) {
                log.info(
                        "Completed request method={} path={} status={} durationMs={}",
                        request.getMethod(),
                        request.getRequestURI(),
                        response.getStatus(),
                        System.currentTimeMillis() - startedAt
                );
            }
        }
    }

    private boolean shouldLog(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || path.startsWith("/api/v1/auth/")
                || path.startsWith("/actuator/health");
    }
}
