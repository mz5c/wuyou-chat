package com.wuyou.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wuyou.chat.api.service.ModelConfigService;
import com.wuyou.chat.service.entity.AiModelConfig;
import com.wuyou.chat.service.exception.BusinessException;
import com.wuyou.chat.service.mapper.AiModelConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 模型配置服务实现
 */
@Slf4j
@Service
public class ModelConfigServiceImpl implements ModelConfigService {

    private final AiModelConfigMapper modelConfigMapper;

    public ModelConfigServiceImpl(AiModelConfigMapper modelConfigMapper) {
        this.modelConfigMapper = modelConfigMapper;
    }

    @Override
    public List<Map<String, Object>> getAllModels() {
        try {
            List<AiModelConfig> configs = modelConfigMapper.selectList(null);
            return configs.stream().map(this::toMap).collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("查询模型配置失败，数据库表可能未初始化", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Map<String, Object>> getEnabledModels() {
        try {
            LambdaQueryWrapper<AiModelConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiModelConfig::getIsEnabled, 1)
                    .orderByAsc(AiModelConfig::getSortOrder);
            List<AiModelConfig> configs = modelConfigMapper.selectList(wrapper);
            return configs.stream().map(c -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", c.getId());
                m.put("name", c.getName());
                m.put("provider", c.getProvider());
                m.put("model", c.getModel());
                m.put("sortOrder", c.getSortOrder());
                return m;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("查询已启用模型配置失败，数据库表可能未初始化", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void addModel(Map<String, Object> model) {
        AiModelConfig config = new AiModelConfig();
        config.setName((String) model.get("name"));
        config.setProvider((String) model.get("provider"));
        config.setApiUrl((String) model.get("apiUrl"));
        config.setApiKey((String) model.get("apiKey"));
        config.setModel((String) model.get("model"));
        config.setIsEnabled(model.get("isEnabled") != null ? (Integer) model.get("isEnabled") : 1);
        config.setSortOrder(model.get("sortOrder") != null ? (Integer) model.get("sortOrder") : 0);
        modelConfigMapper.insert(config);
        log.info("新增模型配置成功：{}", config.getName());
    }

    @Override
    public void updateModel(Long id, Map<String, Object> model) {
        AiModelConfig config = modelConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(404, "模型配置不存在");
        }
        if (model.containsKey("name")) config.setName((String) model.get("name"));
        if (model.containsKey("provider")) config.setProvider((String) model.get("provider"));
        if (model.containsKey("apiUrl")) config.setApiUrl((String) model.get("apiUrl"));
        if (model.containsKey("apiKey")) config.setApiKey((String) model.get("apiKey"));
        if (model.containsKey("model")) config.setModel((String) model.get("model"));
        if (model.containsKey("isEnabled")) config.setIsEnabled((Integer) model.get("isEnabled"));
        if (model.containsKey("sortOrder")) config.setSortOrder((Integer) model.get("sortOrder"));
        modelConfigMapper.updateById(config);
        log.info("更新模型配置成功：{}", id);
    }

    @Override
    public void deleteModel(Long id) {
        modelConfigMapper.deleteById(id);
        log.info("删除模型配置成功：{}", id);
    }

    @Override
    public Map<String, Object> getModelById(Long modelId) {
        AiModelConfig config = modelConfigMapper.selectById(modelId);
        if (config == null || config.getIsEnabled() == 0) {
            return null;
        }
        return toMap(config);
    }

    @Override
    public Map<String, Object> getDefaultModel() {
        try {
            LambdaQueryWrapper<AiModelConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiModelConfig::getIsEnabled, 1)
                    .orderByAsc(AiModelConfig::getSortOrder)
                    .last("LIMIT 1");
            AiModelConfig config = modelConfigMapper.selectOne(wrapper);
            if (config == null) {
                return null;
            }
            return toMap(config);
        } catch (Exception e) {
            log.warn("查询默认模型配置失败，数据库表可能未初始化", e);
            return null;
        }
    }

    /**
     * 将实体转换为 Map
     */
    private Map<String, Object> toMap(AiModelConfig config) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", config.getId());
        m.put("name", config.getName());
        m.put("provider", config.getProvider());
        m.put("apiUrl", config.getApiUrl());
        m.put("apiKey", config.getApiKey());
        m.put("model", config.getModel());
        m.put("isEnabled", config.getIsEnabled());
        m.put("sortOrder", config.getSortOrder());
        m.put("createdAt", config.getCreatedAt());
        m.put("updatedAt", config.getUpdatedAt());
        return m;
    }
}
