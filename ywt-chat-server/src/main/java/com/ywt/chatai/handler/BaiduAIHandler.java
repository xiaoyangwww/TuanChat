package com.ywt.chatai.handler;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ywt.chat.domain.entity.Message;
import com.ywt.chat.domain.entity.msg.MessageExtra;
import com.ywt.chatai.dto.GPTRequestDTO;
import com.ywt.chatai.properties.BaiduProperties;
import com.ywt.chatai.utils.ChatGLM2Utils;
import com.ywt.common.domain.dto.FrequencyControlDTO;
import com.ywt.common.exception.FrequencyControlException;
import com.ywt.common.service.frequencycontrol.FrequencyControlUtil;
import com.ywt.user.domain.vo.Resp.user.UserInfoResp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.ywt.common.service.frequencycontrol.FrequencyControlStrategyFactory.TOTAL_COUNT_WITH_IN_FIX_TIME_FREQUENCY_CONTROLLER;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年05月17日 21:00
 */
@Component
@Slf4j
public class BaiduAIHandler extends AbstractChatAIHandler{

    /**
     * GPTChatAIHandler限流前缀
     */
    private static final String CHAT_FREQUENCY_PREFIX = "BaiduChatAIHandler";

    @Autowired
    private BaiduProperties baiduProperties;

    private static String AI_NAME;

    private static final Random RANDOM = new Random();

    private static final List<String> ERROR_MSG = Arrays.asList(
            "还摸鱼呢？你不下班我还要下班呢。。。。",
            "没给钱，矿工了。。。。",
            "服务器被你们玩儿坏了。。。。",
            "你们这群人，我都不想理你们了。。。。",
            "艾特我那是另外的价钱。。。。",
            "得加钱");

    @Override
    protected void init() {
        super.init();
        if (isUse()) {
            UserInfoResp userInfo = userService.getUserInfo(baiduProperties.getAIUserId());
            if (userInfo == null) {
                log.error("根据AIUserId:{} 找不到用户信息", baiduProperties.getAIUserId());
                throw new RuntimeException("根据AIUserId找不到用户信息");
            }
            if (StringUtils.isBlank(userInfo.getName())) {
                log.warn("根据AIUserId:{} 找到的用户信息没有name", baiduProperties.getAIUserId());
                throw new RuntimeException("根据AIUserId: " + baiduProperties.getAIUserId() + " 找到的用户没有名字");
            }
            AI_NAME = userInfo.getName();
        }
    }

    @Override
    protected boolean isUse() {
        return baiduProperties.isUse();
    }

    @Override
    public Long getChatAIUserId() {
        return baiduProperties.getAIUserId();
    }

    @Override
    protected boolean supports(Message message) {
        if (!baiduProperties.isUse()) {
            return false;
        }
        /* 前端传@信息后取消注释 */

        MessageExtra extra = message.getExtra();
        if (extra == null) {
            return false;
        }
        if (CollectionUtils.isEmpty(extra.getAtUidList())) {
            return false;
        }
        if (!extra.getAtUidList().contains(baiduProperties.getAIUserId())) {
            return false;
        }

        if (StringUtils.isBlank(message.getContent())) {
            return false;
        }
        return StringUtils.contains(message.getContent(), "@" + AI_NAME)
                && StringUtils.isNotBlank(message.getContent().replace(AI_NAME, "").trim());
    }

    @Override
    protected String doChat(Message message) {
        String content = message.getContent().replace("@" + AI_NAME, "").trim();
        Long uid = message.getFromUid();
        try {
            FrequencyControlDTO frequencyControlDTO = new FrequencyControlDTO();
            frequencyControlDTO.setKey(CHAT_FREQUENCY_PREFIX + ":" + uid);
            frequencyControlDTO.setUnit(TimeUnit.MINUTES);
            frequencyControlDTO.setCount(3);
            frequencyControlDTO.setTime(baiduProperties.getLimit());
            return FrequencyControlUtil.executeWithFrequencyControl(TOTAL_COUNT_WITH_IN_FIX_TIME_FREQUENCY_CONTROLLER, frequencyControlDTO, () -> sendRequestToAI(new GPTRequestDTO(content, uid)));
        } catch (FrequencyControlException e) {
            return "你太快了亲爱的~过一会再来找人家~";
        } catch (Throwable e) {
            return "系统开小差啦~~";
        }
    }

    public  String getAccessToken() {
        String apiKey = baiduProperties.getApiKey();
        String secretKey = baiduProperties.getSecretKey();
        String url = String.format(baiduProperties.getTokenUrl(),apiKey,secretKey);
        HttpResponse response = HttpRequest.post(url)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .execute();
        JSONObject jsonResponse = new JSONObject(response.body());
        return jsonResponse.getStr("access_token");
    }

    private String sendRequestToAI(GPTRequestDTO gptRequestDTO) {
        String accessToken = getAccessToken();
        String url = String.format(baiduProperties.getUrl(),accessToken);

        JSONObject payload = new JSONObject();
        payload.put("messages", new JSONObject[]{
                new JSONObject().set("role", "user").set("content", gptRequestDTO.getContent())
        });
        String text = "";
        try {
            HttpResponse response = HttpRequest.post(url)
                    .header("Content-Type", "application/json")
                    .body(payload.toString())
                    .execute();
            JSONObject jsonObject = JSONUtil.parseObj(response.body());
            // 提取 "result" 字段的值
            String result = jsonObject.getStr("result");
            if (StringUtils.isBlank(result)) {
                text = getErrorText();
            }else {
                text = result;
            }
            return text;
        } catch (Exception e) {
            log.warn("glm2 doChat warn:", e);
            return getErrorText();
        }
    }

    private String getErrorText() {
        int index = RANDOM.nextInt(ERROR_MSG.size());
        return ERROR_MSG.get(index);
    }

}
