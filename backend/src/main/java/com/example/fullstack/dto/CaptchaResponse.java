package com.example.fullstack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CaptchaResponse {

    private String captchaKey;

    private String captchaSvg;
}
