/**
 * Interceptor for HTTP request logging using SLF4J.
 */
package br.com.tourapp.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * Interceptor for automatic logging of HTTP requests.
 */
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
    private static final String REQUEST_ID_KEY = "requestId";
    private static final String START_TIME_KEY = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Generate unique ID for the request
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        long startTime = System.currentTimeMillis();

        // Add to MDC for log correlation
        MDC.put(REQUEST_ID_KEY, requestId);
        MDC.put(START_TIME_KEY, String.valueOf(startTime));

        // Log incoming request
        logger.info("🔄 HTTP {} {} from {} | User-Agent: {}",
                request.getMethod(),
                request.getRequestURI(),
                getClientIp(request),
                request.getHeader("User-Agent"));

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        try {
            long startTime = Long.parseLong(MDC.get(START_TIME_KEY));
            long duration = System.currentTimeMillis() - startTime;

            String logMessage = "✅ HTTP {} {} completed | Status: {} | Duration: {}ms";

            if (response.getStatus() >= 400) {
                logMessage = "❌ HTTP {} {} failed | Status: {} | Duration: {}ms";
                logger.warn(logMessage,
                        request.getMethod(),
                        request.getRequestURI(),
                        response.getStatus(),
                        duration);
            } else {
                logger.info(logMessage,
                        request.getMethod(),
                        request.getRequestURI(),
                        response.getStatus(),
                        duration);
            }

            if (ex != null) {
                logger.error("💥 Exception during request processing: {}", ex.getMessage(), ex);
            }

        } finally {
            // Clear MDC
            MDC.clear();
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}