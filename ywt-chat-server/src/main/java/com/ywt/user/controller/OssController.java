package com.ywt.user.controller;

import com.ywt.common.annotation.FrequencyControl;
import com.ywt.common.domain.vo.Resp.ApiResult;
import com.ywt.common.utils.RequestHolder;
import com.ywt.oss.domain.OssReq;
import com.ywt.oss.domain.OssResp;
import com.ywt.user.domain.vo.Req.oss.UploadUrlReq;
import com.ywt.user.service.OssService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 功能描述
 *
 * @author: ywt
 * @date: 2024年05月10日 11:49
 */
@RestController
@RequestMapping("/capi/oss")
@Api(tags = "oss相关接口")
public class OssController {

    @Autowired
    private OssService ossService;

    @GetMapping("/upload/url")
    @ApiOperation("获取临时上传链接")
    @FrequencyControl(time = 5, count = 5, target = FrequencyControl.Target.UID)
    public ApiResult<OssResp> getUploadUrl(@Valid UploadUrlReq req) {
        return ApiResult.success(ossService.getUploadUrl(RequestHolder.get().getUid(),req));
    }

}
