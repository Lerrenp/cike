package com.cike.dto;

import lombok.Data;

/**
 * 注册请求
 */
@Data
public class RegisterRequest {

    private String phone;
    private String code;
    private String nickname;
    private String password;
}
