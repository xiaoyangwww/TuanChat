package com.ywt.user.controller;

import com.ywt.common.domain.vo.Resp.ApiResult;
import com.ywt.common.utils.RequestHolder;
import com.ywt.oss.domain.OssReq;
import com.ywt.oss.domain.OssResp;
import com.ywt.user.domain.vo.Req.oss.UploadUrlReq;
import com.ywt.user.service.OssService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年05月10日 11:49
 */
@RestController
@RequestMapping("/capi/oss")
@Api(tags = "oss相关接口")
public class OssController {

    @Autowired
    private OssService ossService;

    @PostMapping("/upload/url")
    @ApiOperation("获取临时上传链接")
    public ApiResult<OssResp> getUploadUrl(@RequestBody UploadUrlReq req) {
        return ApiResult.success(ossService.getUploadUrl(RequestHolder.get().getUid(),req));
    }

}
