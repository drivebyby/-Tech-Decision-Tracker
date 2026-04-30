package com.example.fullstack.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.fullstack.common.AuthContext;
import com.example.fullstack.common.BusinessException;
import com.example.fullstack.dto.CommitLinkRequest;
import com.example.fullstack.dto.DecisionCreateRequest;
import com.example.fullstack.dto.DecisionDetailResponse;
import com.example.fullstack.dto.DecisionListItem;
import com.example.fullstack.dto.DecisionUpdateRequest;
import com.example.fullstack.dto.SupersedeRequest;
import com.example.fullstack.entity.Decision;
import com.example.fullstack.entity.DecisionCommit;
import com.example.fullstack.entity.DecisionRelation;
import com.example.fullstack.entity.User;
import com.example.fullstack.mapper.DecisionCommitMapper;
import com.example.fullstack.mapper.DecisionMapper;
import com.example.fullstack.mapper.DecisionRelationMapper;
import com.example.fullstack.mapper.UserMapper;
import com.example.fullstack.service.DecisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DecisionServiceImpl implements DecisionService {

    private final DecisionMapper decisionMapper;
    private final DecisionRelationMapper relationMapper;
    private final DecisionCommitMapper commitMapper;
    private final UserMapper userMapper;

    private static String nullIfBlank(String value) {
        return StringUtils.hasText(value) ? value : null;
    }

    @Override
    public IPage<DecisionListItem> pageDecisions(long page, long size, String category, String status) {
        LambdaQueryWrapper<Decision> wrapper = Wrappers.<Decision>lambdaQuery();
        if (StringUtils.hasText(category)) {
            wrapper.eq(Decision::getCategory, category);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Decision::getStatus, status);
        }
        wrapper.orderByDesc(Decision::getId);

        IPage<Decision> decisionPage = decisionMapper.selectPage(new Page<>(page, size), wrapper);

        return decisionPage.convert(d -> {
            long parentCount = relationMapper.selectCount(
                    Wrappers.<DecisionRelation>lambdaQuery().eq(DecisionRelation::getChildId, d.getId()));
            long childCount = relationMapper.selectCount(
                    Wrappers.<DecisionRelation>lambdaQuery().eq(DecisionRelation::getParentId, d.getId()));
            String username = "";
            if (d.getCreatedBy() != null) {
                User user = userMapper.selectById(d.getCreatedBy());
                if (user != null) {
                    username = user.getUsername();
                }
            }
            return new DecisionListItem(d.getId(), d.getTitle(), d.getChosenOption(),
                    d.getStatus(), d.getCategory(), username, d.getCreatedAt(), (int) parentCount, (int) childCount);
        });
    }

    @Override
    public DecisionDetailResponse getDetail(Long id) {
        Decision decision = decisionMapper.selectById(id);
        if (decision == null) {
            throw new BusinessException(404, "决策不存在");
        }

        String username = "";
        if (decision.getCreatedBy() != null) {
            User user = userMapper.selectById(decision.getCreatedBy());
            if (user != null) {
                username = user.getUsername();
            }
        }

        DecisionDetailResponse detail = new DecisionDetailResponse();
        detail.setId(decision.getId());
        detail.setTitle(decision.getTitle());
        detail.setContext(decision.getContext());
        detail.setOptions(decision.getOptions());
        detail.setChosenOption(decision.getChosenOption());
        detail.setReason(decision.getReason());
        detail.setImpact(decision.getImpact());
        detail.setStatus(decision.getStatus());
        detail.setCategory(decision.getCategory());
        detail.setCreatedBy(username);
        detail.setCreatedAt(decision.getCreatedAt());
        detail.setUpdatedAt(decision.getUpdatedAt());

        detail.setParents(buildChainNodes(id, true));
        detail.setChildren(buildChainNodes(id, false));

        List<DecisionCommit> commits = commitMapper.selectList(
                Wrappers.<DecisionCommit>lambdaQuery().eq(DecisionCommit::getDecisionId, id));
        detail.setCommits(commits.stream().map(c -> new DecisionDetailResponse.CommitInfo(
                c.getId(), c.getCommitHash(), c.getCommitMessage(), c.getFilePath(), c.getRepoUrl()))
                .collect(Collectors.toList()));

        return detail;
    }

    @Override
    @Transactional
    public DecisionDetailResponse create(DecisionCreateRequest request) {
        Decision decision = new Decision();
        decision.setTitle(request.getTitle());
        decision.setContext(nullIfBlank(request.getContext()));
        decision.setOptions(nullIfBlank(request.getOptions()));
        decision.setChosenOption(nullIfBlank(request.getChosenOption()));
        decision.setReason(nullIfBlank(request.getReason()));
        decision.setImpact(nullIfBlank(request.getImpact()));
        decision.setStatus("active");
        decision.setCategory(request.getCategory());
        decision.setCreatedBy(AuthContext.getCurrentUserId());
        decision.setCreatedAt(LocalDateTime.now());
        decision.setUpdatedAt(LocalDateTime.now());
        decisionMapper.insert(decision);
        return getDetail(decision.getId());
    }

    @Override
    @Transactional
    public DecisionDetailResponse update(Long id, DecisionUpdateRequest request) {
        Decision decision = decisionMapper.selectById(id);
        if (decision == null) {
            throw new BusinessException(404, "决策不存在");
        }
        decision.setTitle(request.getTitle());
        decision.setContext(nullIfBlank(request.getContext()));
        decision.setOptions(nullIfBlank(request.getOptions()));
        decision.setChosenOption(nullIfBlank(request.getChosenOption()));
        decision.setReason(nullIfBlank(request.getReason()));
        decision.setImpact(nullIfBlank(request.getImpact()));
        decision.setCategory(request.getCategory());
        if (StringUtils.hasText(request.getStatus())) {
            decision.setStatus(request.getStatus());
        }
        decision.setUpdatedAt(LocalDateTime.now());
        decisionMapper.updateById(decision);
        return getDetail(id);
    }

    @Override
    @Transactional
    public DecisionDetailResponse supersede(SupersedeRequest request) {
        Decision parent = decisionMapper.selectById(request.getParentId());
        if (parent == null) {
            throw new BusinessException(404, "被推翻的决策不存在");
        }
        if ("superseded".equals(parent.getStatus())) {
            throw new BusinessException(400, "该决策已被推翻，不能重复推翻");
        }

        Decision child = new Decision();
        child.setTitle(request.getTitle());
        child.setContext(nullIfBlank(request.getContext()));
        child.setOptions(nullIfBlank(request.getOptions()));
        child.setChosenOption(nullIfBlank(request.getChosenOption()));
        child.setReason(nullIfBlank(request.getReason()));
        child.setImpact(nullIfBlank(request.getImpact()));
        child.setStatus("active");
        child.setCategory(request.getCategory());
        child.setCreatedBy(AuthContext.getCurrentUserId());
        child.setCreatedAt(LocalDateTime.now());
        child.setUpdatedAt(LocalDateTime.now());
        decisionMapper.insert(child);

        parent.setStatus("superseded");
        parent.setUpdatedAt(LocalDateTime.now());
        decisionMapper.updateById(parent);

        DecisionRelation relation = new DecisionRelation();
        relation.setParentId(parent.getId());
        relation.setChildId(child.getId());
        relation.setRelationType("supersedes");
        relation.setCreatedAt(LocalDateTime.now());
        relationMapper.insert(relation);

        return getDetail(child.getId());
    }

    @Override
    public List<DecisionListItem> getTimeline(String category) {
        LambdaQueryWrapper<Decision> wrapper = Wrappers.<Decision>lambdaQuery()
                .orderByAsc(Decision::getCreatedAt);
        if (StringUtils.hasText(category)) {
            wrapper.eq(Decision::getCategory, category);
        }
        List<Decision> decisions = decisionMapper.selectList(wrapper);

        return decisions.stream().map(d -> {
            long parentCount = relationMapper.selectCount(
                    Wrappers.<DecisionRelation>lambdaQuery().eq(DecisionRelation::getChildId, d.getId()));
            long childCount = relationMapper.selectCount(
                    Wrappers.<DecisionRelation>lambdaQuery().eq(DecisionRelation::getParentId, d.getId()));
            String username = "";
            if (d.getCreatedBy() != null) {
                User user = userMapper.selectById(d.getCreatedBy());
                if (user != null) {
                    username = user.getUsername();
                }
            }
            return new DecisionListItem(d.getId(), d.getTitle(), d.getChosenOption(),
                    d.getStatus(), d.getCategory(), username, d.getCreatedAt(), (int) parentCount, (int) childCount);
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getGraph() {
        List<Decision> decisions = decisionMapper.selectList(Wrappers.<Decision>lambdaQuery()
                .orderByAsc(Decision::getCreatedAt));
        List<DecisionRelation> relations = relationMapper.selectList(null);

        List<Map<String, Object>> nodes = decisions.stream().map(d -> {
            Map<String, Object> node = new HashMap<>();
            node.put("id", d.getId().toString());
            node.put("name", d.getTitle());
            node.put("category", d.getCategory());
            node.put("status", d.getStatus());
            return node;
        }).collect(Collectors.toList());

        List<Map<String, Object>> edges = relations.stream().map(r -> {
            Map<String, Object> edge = new HashMap<>();
            edge.put("source", r.getParentId().toString());
            edge.put("target", r.getChildId().toString());
            edge.put("relationType", r.getRelationType());
            return edge;
        }).collect(Collectors.toList());

        Map<String, Object> graph = new HashMap<>();
        graph.put("nodes", nodes);
        graph.put("edges", edges);
        return graph;
    }

    @Override
    @Transactional
    public DecisionDetailResponse.CommitInfo linkCommit(Long decisionId, CommitLinkRequest request) {
        Decision decision = decisionMapper.selectById(decisionId);
        if (decision == null) {
            throw new BusinessException(404, "决策不存在");
        }

        DecisionCommit commit = new DecisionCommit();
        commit.setDecisionId(decisionId);
        commit.setCommitHash(request.getCommitHash());
        commit.setCommitMessage(request.getCommitMessage());
        commit.setFilePath(request.getFilePath());
        commit.setRepoUrl(request.getRepoUrl());
        commit.setCreatedAt(LocalDateTime.now());
        commitMapper.insert(commit);

        return new DecisionDetailResponse.CommitInfo(
                commit.getId(), commit.getCommitHash(), commit.getCommitMessage(),
                commit.getFilePath(), commit.getRepoUrl());
    }

    private List<DecisionDetailResponse.ChainNode> buildChainNodes(Long decisionId, boolean isParent) {
        List<DecisionRelation> relations;
        if (isParent) {
            relations = relationMapper.selectList(
                    Wrappers.<DecisionRelation>lambdaQuery().eq(DecisionRelation::getChildId, decisionId));
        } else {
            relations = relationMapper.selectList(
                    Wrappers.<DecisionRelation>lambdaQuery().eq(DecisionRelation::getParentId, decisionId));
        }

        List<DecisionDetailResponse.ChainNode> nodes = new ArrayList<>();
        for (DecisionRelation relation : relations) {
            Long relatedId = isParent ? relation.getParentId() : relation.getChildId();
            Decision related = decisionMapper.selectById(relatedId);
            if (related != null) {
                nodes.add(new DecisionDetailResponse.ChainNode(
                        related.getId(), related.getTitle(), relation.getRelationType()));
            }
        }
        return nodes;
    }
}
