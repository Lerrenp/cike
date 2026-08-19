package com.cike.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cike.common.BusinessException;
import com.cike.common.JwtUtil;
import com.cike.common.LoginVO;
import com.cike.dto.LoginRequest;
import com.cike.dto.RegisterRequest;
import com.cike.dto.SmsCodeRequest;
import com.cike.entity.User;
import com.cike.mapper.UserMapper;
import com.cike.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 认证服务：短信验证码、注册、登录、退出
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public Map<String, String> sendSmsCode(SmsCodeRequest request) {
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new BusinessException(400, "手机号不能为空");
        }
        // 开发环境：直接生成本地验证码并明文返回（无 Redis / 短信服务）
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
        Map<String, String> data = new HashMap<>();
        data.put("code", code);
        return data;
    }

    @Override
    public LoginVO register(RegisterRequest request) {
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new BusinessException(400, "手机号不能为空");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new BusinessException(400, "密码长度不能少于6位");
        }
        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new BusinessException(400, "验证码不能为空");
        }
        if (request.getNickname() == null || request.getNickname().isBlank()) {
            throw new BusinessException(400, "昵称不能为空");
        }
        Long exists = userMapper.selectCount(Wrappers.<User>lambdaQuery().eq(User::getPhone, request.getPhone()));
        if (exists != null && exists > 0) {
            throw new BusinessException(400, "该手机号已注册");
        }

        User user = new User();
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setAvatar("");
        user.setBio("");
        user.setNoteCount(0);
        user.setLikeTotal(0);
        userMapper.insert(user);

        String token = jwtUtil.createToken(user.getId());
        return new LoginVO(token, user);
    }

    @Override
    public LoginVO login(LoginRequest request) {
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new BusinessException(400, "手机号不能为空");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BusinessException(400, "密码不能为空");
        }
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getPhone, request.getPhone()));
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "密码错误");
        }
        String token = jwtUtil.createToken(user.getId());
        return new LoginVO(token, user);
    }

    @Override
    public void logout() {
        // 无状态 JWT，无需服务端处理
    }
}
