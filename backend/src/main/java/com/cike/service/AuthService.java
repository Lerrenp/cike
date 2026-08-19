package com.cike.service;

import com.cike.common.LoginVO;
import com.cike.dto.LoginRequest;
import com.cike.dto.RegisterRequest;
import com.cike.dto.SmsCodeRequest;

import java.util.Map;

public interface AuthService {

    /** 发送短信验证码，开发环境验证码明文返回 */
    Map<String, String> sendSmsCode(SmsCodeRequest request);

    LoginVO register(RegisterRequest request);

    LoginVO login(LoginRequest request);

    void logout();
}
