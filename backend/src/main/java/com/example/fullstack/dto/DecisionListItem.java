package com.example.fullstack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecisionListItem {

    private Long id;
    private String title;
    private String chosenOption;
    private String status;
    private String category;
    private String createdBy;
    private LocalDateTime createdAt;
    private int parentCount;
    private int childCount;
}
