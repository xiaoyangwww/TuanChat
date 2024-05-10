package com.ywt.user.service.impl;

import com.ywt.common.utils.AssertUtil;
import com.ywt.oss.MinIOTemplate;
import com.ywt.oss.domain.OssReq;
import com.ywt.oss.domain.OssResp;
import com.ywt.user.domain.enums.OssSceneEnum;
import com.ywt.user.domain.vo.Req.oss.UploadUrlReq;
import com.ywt.user.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年05月10日 11:51
 */
@Service
public class OssServiceImpl implements OssService {

    @Autowired
    private MinIOTemplate minIOTemplate;

    @Override
    public OssResp getUploadUrl(Long uid, UploadUrlReq req) {
        OssSceneEnum sceneEnum = OssSceneEnum.of(req.getScene());
        AssertUtil.isNotEmpty(sceneEnum, "场景有误");
        OssReq ossReq = OssReq.builder()
                .fileName(req.getFileName())
                .filePath(sceneEnum.getPath())
                .uid(uid)
                .build();
        return minIOTemplate.getPreSignedObjectUrl(ossReq);
    }
}
