package com.example.fullstack.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommitLinkRequest {

    @NotBlank(message = "Commit hash 不能为空")
    private String commitHash;

    private String commitMessage;

    private String filePath;

    private String repoUrl;
}
