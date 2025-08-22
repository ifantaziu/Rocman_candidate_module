package org.rocman.candidate.loggingConfig;

import org.apache.logging.log4j.ThreadContext;
import org.rocman.candidate.entities.Candidate;
import org.rocman.candidate.utils.LogMaskingUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.UUID;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) {
            ip = request.getRemoteAddr();
        }

        String requestId = UUID.randomUUID().toString();

        String userId = "guest";
        String userEmail = "N/A";

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            Candidate candidate = (Candidate) auth.getPrincipal();
            userId = String.valueOf(candidate.getId());
            userEmail = LogMaskingUtil.maskEmail(candidate.getEmail());
        }

        ThreadContext.put("ip", ip);
        ThreadContext.put("requestId", requestId);
        ThreadContext.put("userId", userId);
        ThreadContext.put("userEmail", userEmail);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ThreadContext.clearAll();
    }
}