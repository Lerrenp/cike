package com.cike.common;

import com.cike.entity.User;
import lombok.Data;

/**
 * 登录结果 VO
 */
@Data
public class LoginVO {

    private String token;
    private User user;

    public LoginVO(String token, User user) {
        this.token = token;
        this.user = user;
    }
}
