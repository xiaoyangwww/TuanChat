package com.ywt.chat.service.strategy.mark;

import com.ywt.common.exception.CommonErrorEnum;
import com.ywt.common.utils.AssertUtil;
import io.swagger.models.auth.In;

import java.util.HashMap;
import java.util.Map;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年05月12日 9:41
 */
public class MsgMarkFactory {

    public static Map<Integer,AbstractMsgMarkStrategy> STRATEGY_MAP = new HashMap<>();

    public static void register(Integer type, AbstractMsgMarkStrategy strategy) {
        STRATEGY_MAP.put(type,strategy);
    }

    public static AbstractMsgMarkStrategy getStrategyNoNull(Integer type) {
        AbstractMsgMarkStrategy strategy = STRATEGY_MAP.get(type);
        AssertUtil.isNotEmpty(strategy, CommonErrorEnum.PARAM_VALID);
        return strategy;
    }
}
