package com.cike.dto;

import lombok.Data;

/**
 * 发送短信验证码请求
 */
@Data
public class SmsCodeRequest {

    private String phone;
    /** register 注册 / login 登录 */
    private String scene;
}
