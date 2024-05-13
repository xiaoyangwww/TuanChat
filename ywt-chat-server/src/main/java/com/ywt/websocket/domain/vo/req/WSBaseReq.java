package com.ywt.websocket.domain.vo.req;

import io.swagger.models.auth.In;
import lombok.Data;

/**
 * 功能描述
 * ws的基本返回信息体
 * @author: ywt
 * @date: 2024年04月15日 16:32
 */
@Data
public class WSBaseReq<T> {

    /**
     * 接受前端数据的类型
     * @see com.ywt.websocket.domain.enums.WSReqTypeEnum
     */
    private Integer type;

    private T data;
}
