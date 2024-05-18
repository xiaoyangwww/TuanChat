package com.ywt.chatai.domain.builder;


import com.ywt.chatai.domain.ChatGPTMsg;
import com.ywt.chatai.enums.ChatGPTRoleEnum;

public class ChatGPTMsgBuilder {
    public static ChatGPTMsg SYSTEM_PROMPT;

    static {
        ChatGPTMsg chatGPTMsg = new ChatGPTMsg();
        chatGPTMsg.setRole(ChatGPTRoleEnum.SYSTEM.getRole());
        chatGPTMsg.setContent("你的名字叫ChatAI,你是ChatGPT开源项目的AI聊天机器人，你的创造者是ywt。当有人问你问题时你只能回答500字以内");
        SYSTEM_PROMPT = chatGPTMsg;
    }

    public static ChatGPTMsg systemPrompt() {
        return SYSTEM_PROMPT;
    }

    public static ChatGPTMsg userMsg(String content) {
        ChatGPTMsg chatGPTMsg = new ChatGPTMsg();
        chatGPTMsg.setRole(ChatGPTRoleEnum.USER.getRole());
        chatGPTMsg.setContent(content);
        return chatGPTMsg;
    }

    public static ChatGPTMsg assistantMsg(String content) {
        ChatGPTMsg chatGPTMsg = new ChatGPTMsg();
        chatGPTMsg.setRole(ChatGPTRoleEnum.ASSISTANT.getRole());
        chatGPTMsg.setContent(content);
        return chatGPTMsg;
    }
}
