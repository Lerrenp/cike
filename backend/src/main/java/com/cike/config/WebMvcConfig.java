package com.cike.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：注册鉴权拦截器并配置放行路径
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    /** 无需登录可访问的路径（注意：/notes 列表/详情仅对 GET 匿名放行，见 AuthInterceptor；POST 发布 / DELETE 需登录） */
    private static final String[] PUBLIC_PATHS = {
            "/auth/sms/code",
            "/auth/register",
            "/auth/login",
            "/users",
            "/users/*",
            "/topics"
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(PUBLIC_PATHS);
    }
}
