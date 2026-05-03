package com.wuyou.chat.service.controller;

import com.wuyou.chat.api.dto.Result;
import com.wuyou.chat.api.service.ModelConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 模型配置前端控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/models")
@Tag(name = "模型接口", description = "前端模型配置查询")
public class ModelController {

    private final ModelConfigService modelConfigService;

    public ModelController(ModelConfigService modelConfigService) {
        this.modelConfigService = modelConfigService;
    }

    @GetMapping("/enabled")
    @Operation(summary = "获取已启用的模型配置")
    public Result<List<Map<String, Object>>> getEnabled() {
        return Result.success(modelConfigService.getEnabledModels());
    }
}
