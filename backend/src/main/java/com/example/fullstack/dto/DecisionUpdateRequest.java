package com.example.fullstack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DecisionUpdateRequest {

    @NotBlank(message = "决策标题不能为空")
    @Size(max = 255, message = "标题不能超过 255 个字符")
    private String title;

    private String context;

    private String options;

    private String chosenOption;

    private String reason;

    private String impact;

    @NotBlank(message = "分类不能为空")
    private String category;

    private String status;
}
