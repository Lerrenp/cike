package com.cike.controller;

import com.cike.common.Result;
import com.cike.dto.LoginRequest;
import com.cike.dto.RegisterRequest;
import com.cike.dto.SmsCodeRequest;
import com.cike.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证模块 /api/v1/auth
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 1. 发送短信验证码 */
    @PostMapping("/sms/code")
    public Result<Map<String, String>> sendSmsCode(@RequestBody SmsCodeRequest request) {
        return Result.ok("发送成功", authService.sendSmsCode(request));
    }

    /** 2. 注册 */
    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterRequest request) {
        return Result.ok("注册成功", authService.register(request));
    }

    /** 3. 登录 */
    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginRequest request) {
        return Result.ok("登录成功", authService.login(request));
    }

    /** 4. 退出登录 */
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.ok("退出成功");
    }
}
