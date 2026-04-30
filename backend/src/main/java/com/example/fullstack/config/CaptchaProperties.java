package com.example.fullstack.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.captcha")
public class CaptchaProperties {

    private int expireMinutes = 5;
}
