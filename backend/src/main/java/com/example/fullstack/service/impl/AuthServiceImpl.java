package com.example.fullstack.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.fullstack.common.AuthContext;
import com.example.fullstack.common.BusinessException;
import com.example.fullstack.config.CaptchaProperties;
import com.example.fullstack.dto.CaptchaResponse;
import com.example.fullstack.dto.LoginRequest;
import com.example.fullstack.dto.LoginResponse;
import com.example.fullstack.dto.RegisterRequest;
import com.example.fullstack.dto.UserProfileResponse;
import com.example.fullstack.entity.User;
import com.example.fullstack.service.AuthService;
import com.example.fullstack.service.UserService;
import com.example.fullstack.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final CaptchaProperties captchaProperties;

    private final Map<String, CaptchaRecord> captchaStore = new ConcurrentHashMap<>();

    @Override
    public CaptchaResponse generateCaptcha() {
        String captchaKey = UUID.randomUUID().toString().replace("-", "");
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(140, 48, 4, 40);
        captchaStore.put(captchaKey, new CaptchaRecord(
                captcha.getCode().toLowerCase(Locale.ROOT),
                LocalDateTime.now().plusMinutes(captchaProperties.getExpireMinutes())
        ));
        cleanExpiredCaptchas();
        return new CaptchaResponse(captchaKey, captcha.getImageBase64Data());
    }

    @Override
    public LoginResponse register(RegisterRequest request) {
        verifyCaptcha(request.getCaptchaKey(), request.getCaptchaCode());
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(400, "两次输入的密码不一致");
        }

        User byUsername = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .last("limit 1"));
        if (byUsername != null) {
            throw new BusinessException(400, "用户名已存在");
        }

        User byEmail = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail())
                .last("limit 1"));
        if (byEmail != null) {
            throw new BusinessException(400, "邮箱已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userService.save(user);
        log.info("用户注册成功: username={}, email={}", user.getUsername(), user.getEmail());
        return buildLoginResponse(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        verifyCaptcha(request.getCaptchaKey(), request.getCaptchaCode());
        User user = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .last("limit 1"));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("登录失败: username={}", request.getUsername());
            throw new BusinessException(400, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "账号已被禁用");
        }

        log.info("用户登录成功: username={}", user.getUsername());
        return buildLoginResponse(user);
    }

    @Override
    public UserProfileResponse currentUser() {
        Long userId = AuthContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return toProfile(user);
    }

    private LoginResponse buildLoginResponse(User user) {
        return new LoginResponse(jwtTokenUtil.generateToken(user.getId(), user.getUsername()), toProfile(user));
    }

    private UserProfileResponse toProfile(User user) {
        return new UserProfileResponse(user.getId(), user.getUsername(), user.getEmail());
    }

    private void verifyCaptcha(String captchaKey, String captchaCode) {
        if (!StringUtils.hasText(captchaKey) || !StringUtils.hasText(captchaCode)) {
            throw new BusinessException(400, "请输入验证码");
        }

        CaptchaRecord record = captchaStore.remove(captchaKey);
        if (record == null || record.expireAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(400, "验证码已失效，请刷新后重试");
        }
        if (!record.code().equals(captchaCode.trim().toLowerCase(Locale.ROOT))) {
            throw new BusinessException(400, "验证码错误");
        }
    }

    private void cleanExpiredCaptchas() {
        LocalDateTime now = LocalDateTime.now();
        captchaStore.entrySet().removeIf(entry -> entry.getValue().expireAt().isBefore(now));
    }

    private record CaptchaRecord(String code, LocalDateTime expireAt) {
    }
}
