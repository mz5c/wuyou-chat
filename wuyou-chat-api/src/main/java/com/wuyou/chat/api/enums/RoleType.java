package com.wuyou.chat.api.enums;

/**
 * AI 角色类型枚举
 */
public enum RoleType {

    GENERAL("通用助手", "你是一个智能助手，请回答用户的问题。"),
    TRANSLATOR("翻译助手", "你是一个专业的翻译助手。请将用户输入的内容准确翻译成目标语言，保持原意、语气和风格。"),
    CODE_REVIEW("代码审查", "你是一个资深的代码审查专家。请审查用户提交的代码，指出潜在 Bug、性能问题、安全漏洞和改进建议。"),
    WRITER("写作助手", "你是一个专业的写作助手。帮助用户润色文章、撰写邮件、起草文案，确保内容清晰、专业、有说服力。");

    private final String displayName;
    private final String systemPrompt;

    RoleType(String displayName, String systemPrompt) {
        this.displayName = displayName;
        this.systemPrompt = systemPrompt;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }
}
