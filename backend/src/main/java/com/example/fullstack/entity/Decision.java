package com.example.fullstack.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("decision")
public class Decision {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String context;

    private String options;

    private String chosenOption;

    private String reason;

    private String impact;

    private String status;

    private String category;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
