package com.wuyou.chat.service.controller;

import com.wuyou.chat.api.dto.Result;
import com.wuyou.chat.api.service.ModelConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 后台模型配置管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/models")
@Tag(name = "模型管理", description = "后台模型配置管理")
public class AdminModelConfigController {

    private final ModelConfigService modelConfigService;

    public AdminModelConfigController(ModelConfigService modelConfigService) {
        this.modelConfigService = modelConfigService;
    }

    @GetMapping
    @Operation(summary = "获取所有模型配置")
    public Result<List<Map<String, Object>>> getAll() {
        return Result.success(modelConfigService.getAllModels());
    }

    @PostMapping
    @Operation(summary = "新增模型配置")
    public Result<Void> add(@RequestBody Map<String, Object> model) {
        modelConfigService.addModel(model);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新模型配置")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> model) {
        modelConfigService.updateModel(id, model);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除模型配置")
    public Result<Void> delete(@PathVariable Long id) {
        modelConfigService.deleteModel(id);
        return Result.success();
    }
}
