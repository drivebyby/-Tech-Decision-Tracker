package com.example.fullstack.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.fullstack.common.ApiResponse;
import com.example.fullstack.dto.CommitLinkRequest;
import com.example.fullstack.dto.DecisionCreateRequest;
import com.example.fullstack.dto.DecisionDetailResponse;
import com.example.fullstack.dto.DecisionListItem;
import com.example.fullstack.dto.DecisionUpdateRequest;
import com.example.fullstack.dto.SupersedeRequest;
import com.example.fullstack.service.DecisionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/decisions")
public class DecisionController {

    private final DecisionService decisionService;

    @GetMapping
    public ApiResponse<IPage<DecisionListItem>> pageDecisions(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status
    ) {
        return ApiResponse.ok(decisionService.pageDecisions(page, size, category, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<DecisionDetailResponse> getDetail(@PathVariable Long id) {
        return ApiResponse.ok(decisionService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<DecisionDetailResponse> create(@Valid @RequestBody DecisionCreateRequest request) {
        return ApiResponse.ok(decisionService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<DecisionDetailResponse> update(@PathVariable Long id, @Valid @RequestBody DecisionUpdateRequest request) {
        return ApiResponse.ok(decisionService.update(id, request));
    }

    @PostMapping("/{id}/supersede")
    public ApiResponse<DecisionDetailResponse> supersede(@PathVariable Long id, @Valid @RequestBody SupersedeRequest request) {
        request.setParentId(id);
        return ApiResponse.ok(decisionService.supersede(request));
    }

    @GetMapping("/timeline")
    public ApiResponse<List<DecisionListItem>> getTimeline(@RequestParam(required = false) String category) {
        return ApiResponse.ok(decisionService.getTimeline(category));
    }

    @GetMapping("/graph")
    public ApiResponse<Map<String, Object>> getGraph() {
        return ApiResponse.ok(decisionService.getGraph());
    }

    @PostMapping("/{id}/commits")
    public ApiResponse<DecisionDetailResponse.CommitInfo> linkCommit(
            @PathVariable Long id, @Valid @RequestBody CommitLinkRequest request) {
        return ApiResponse.ok(decisionService.linkCommit(id, request));
    }
}
