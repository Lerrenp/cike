package com.cike.config;

import com.cike.common.JwtUtil;
import com.cike.common.UserContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** Bearer/cookie JWT 鉴权拦截器。 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        String path = request.getServletPath();
        if (HttpMethod.GET.matches(request.getMethod())
                && (path.equals("/notes") || path.matches("/notes/\\d+"))) {
            return true;
        }

        Long userId = parseBearer(request.getHeader("Authorization"));
        if (userId == null) {
            userId = parseCookie(request, "cike_token");
        }
        if (userId != null) {
            UserContext.setUserId(userId);
            return true;
        }
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已失效\",\"data\":null}");
        return false;
    }

    private Long parseBearer(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return jwtUtil.parseUserId(header.substring(7));
    }

    private Long parseCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return jwtUtil.parseUserId(cookie.getValue());
            }
        }
        return null;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
