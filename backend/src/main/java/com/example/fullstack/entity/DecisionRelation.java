package com.example.fullstack.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("decision_relation")
public class DecisionRelation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parentId;

    private Long childId;

    private String relationType;

    private LocalDateTime createdAt;
}
