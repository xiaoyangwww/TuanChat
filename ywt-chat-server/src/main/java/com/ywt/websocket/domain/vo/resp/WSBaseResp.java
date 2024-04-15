package com.ywt.websocket.domain.vo.resp;

import com.ywt.websocket.domain.enums.WSRespTypeEnum;
import lombok.Data;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年04月15日 16:39
 */
@Data
public class WSBaseResp<T> {

    /**
     * ws推送给前端的消息
     *
     * @see WSRespTypeEnum
     */
    private Integer type;

    private T data;


}
