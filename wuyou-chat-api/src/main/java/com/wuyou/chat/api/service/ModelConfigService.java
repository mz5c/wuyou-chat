package com.wuyou.chat.api.service;

import java.util.List;
import java.util.Map;

/**
 * AI 模型配置服务接口
 */
public interface ModelConfigService {

    /**
     * 获取所有模型配置
     *
     * @return 模型配置列表
     */
    List<Map<String, Object>> getAllModels();

    /**
     * 获取已启用的模型配置
     *
     * @return 已启用的模型配置列表
     */
    List<Map<String, Object>> getEnabledModels();

    /**
     * 新增模型配置
     *
     * @param model 模型配置
     */
    void addModel(Map<String, Object> model);

    /**
     * 更新模型配置
     *
     * @param id    配置 ID
     * @param model 模型配置
     */
    void updateModel(Long id, Map<String, Object> model);

    /**
     * 删除模型配置
     *
     * @param id 配置 ID
     */
    void deleteModel(Long id);

    /**
     * 根据 ID 获取模型配置
     *
     * @param modelId 配置 ID
     * @return 模型配置
     */
    Map<String, Object> getModelById(Long modelId);

    /**
     * 获取默认模型配置（排序最前的已启用配置）
     *
     * @return 默认模型配置
     */
    Map<String, Object> getDefaultModel();
}
