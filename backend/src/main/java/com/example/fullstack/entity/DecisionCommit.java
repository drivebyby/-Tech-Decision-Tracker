package com.example.fullstack.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("decision_commit")
public class DecisionCommit {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long decisionId;

    private String commitHash;

    private String commitMessage;

    private String filePath;

    private String repoUrl;

    private LocalDateTime createdAt;
}
