package com.example.fullstack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecisionDetailResponse {

    private Long id;
    private String title;
    private String context;
    private String options;
    private String chosenOption;
    private String reason;
    private String impact;
    private String status;
    private String category;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<ChainNode> parents;
    private List<ChainNode> children;
    private List<CommitInfo> commits;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChainNode {
        private Long id;
        private String title;
        private String relationType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommitInfo {
        private Long id;
        private String commitHash;
        private String commitMessage;
        private String filePath;
        private String repoUrl;
    }
}
