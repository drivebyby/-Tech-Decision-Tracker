package com.example.fullstack.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.fullstack.dto.CommitLinkRequest;
import com.example.fullstack.dto.DecisionCreateRequest;
import com.example.fullstack.dto.DecisionDetailResponse;
import com.example.fullstack.dto.DecisionListItem;
import com.example.fullstack.dto.DecisionUpdateRequest;
import com.example.fullstack.dto.SupersedeRequest;

import java.util.List;
import java.util.Map;

public interface DecisionService {

    IPage<DecisionListItem> pageDecisions(long page, long size, String category, String status);

    DecisionDetailResponse getDetail(Long id);

    DecisionDetailResponse create(DecisionCreateRequest request);

    DecisionDetailResponse update(Long id, DecisionUpdateRequest request);

    DecisionDetailResponse supersede(SupersedeRequest request);

    List<DecisionListItem> getTimeline(String category);

    Map<String, Object> getGraph();

    DecisionDetailResponse.CommitInfo linkCommit(Long decisionId, CommitLinkRequest request);
}
