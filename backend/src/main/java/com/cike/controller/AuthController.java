package com.cike.controller;

import com.cike.common.Result;
import com.cike.common.UserContext;
import com.cike.dto.LoginRequest;
import com.cike.dto.RegisterRequest;
import com.cike.dto.SmsCodeRequest;
import com.cike.service.AuthService;
import com.cike.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final int COOKIE_MAX_AGE = 60 * 60 * 24 * 7;
    private final AuthService authService;
    private final UserService userService;


    @PostMapping("/sms/code")
    public Result<Map<String, String>> sendSmsCode(@RequestBody SmsCodeRequest request) {
        return Result.ok("发送成功", authService.sendSmsCode(request));
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterRequest request, HttpServletResponse response) {
        var login = authService.register(request);
        writeAuthCookie(response, login.getToken(), COOKIE_MAX_AGE);
        return Result.ok("注册成功", login);
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        var login = authService.login(request);
        writeAuthCookie(response, login.getToken(), COOKIE_MAX_AGE);
        return Result.ok("登录成功", login);
    }

    @PostMapping("/session")
    public Result<?> session() {
        return Result.ok(userService.getUser(UserContext.requireUserId()));
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletResponse response) {
        authService.logout();
        writeAuthCookie(response, "", 0);
        return Result.ok("退出成功");
    }

    private void writeAuthCookie(HttpServletResponse response, String token, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from("cike_token", token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
