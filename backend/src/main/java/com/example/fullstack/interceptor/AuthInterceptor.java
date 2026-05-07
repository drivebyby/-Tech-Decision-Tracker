package com.example.fullstack.interceptor;

import com.example.fullstack.common.AuthContext;
import com.example.fullstack.common.BusinessException;
import com.example.fullstack.util.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.debug("请求: {} {}", request.getMethod(), request.getRequestURI());
        String authorization = request.getHeader("Authorization");
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            log.warn("未认证请求: {} {}", request.getMethod(), request.getRequestURI());
            throw new BusinessException(401, "请先登录");
        }

        Claims claims = jwtTokenUtil.parseToken(authorization.substring(7));
        Object userId = claims.get("userId");
        if (userId == null) {
            log.warn("Token无效: {}", request.getRequestURI());
            throw new BusinessException(401, "登录状态无效");
        }

        AuthContext.setCurrentUserId(Long.parseLong(userId.toString()));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }
}
