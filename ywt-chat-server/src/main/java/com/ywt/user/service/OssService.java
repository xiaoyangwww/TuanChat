package com.ywt.user.service;

import com.ywt.oss.domain.OssReq;
import com.ywt.oss.domain.OssResp;
import com.ywt.user.domain.vo.Req.oss.UploadUrlReq;

public interface OssService {

    /**
     * 获取临时上传链接
     *
     * @param uid
     * @param req
     * @return
     */
    OssResp getUploadUrl(Long uid, UploadUrlReq req);
}
