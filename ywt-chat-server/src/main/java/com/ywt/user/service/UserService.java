package com.ywt.user.service;

import com.ywt.user.domain.dto.ItemInfoDTO;
import com.ywt.user.domain.dto.SummeryInfoDTO;
import com.ywt.user.domain.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ywt.user.domain.vo.Req.user.ItemInfoReq;
import com.ywt.user.domain.vo.Req.user.ModifyNameReq;
import com.ywt.user.domain.vo.Req.user.SummeryInfoReq;
import com.ywt.user.domain.vo.Req.user.WearingBadgeReq;
import com.ywt.user.domain.vo.Resp.user.BadgeResp;
import com.ywt.user.domain.vo.Resp.user.UserInfoResp;

import java.util.List;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author ywt
 * @since 2024-04-15
 */
public interface UserService {

    /**
     * 用户注册
     */
    Long register(User user);

    /**
     *获取用户信息
     */
    UserInfoResp getUserInfo(Long uid);

    /**
     * 修改用户名
     */
    void modifyName(ModifyNameReq modifyNameReq);

    /**
     * 获取可选徽章列表
     */
    List<BadgeResp> badges(Long uid);

    /**
     * 拉黑用户
     * @param uid
     */
    void black(Long uid);

    /**
     * 用户聚合信息-返回的代表需要刷新的
     * @param req
     * @return
     */
    List<SummeryInfoDTO> getSummeryUserInfo(SummeryInfoReq req);

    /**
     * 佩戴徽章
     * @param uid
     * @param req
     */
    void wearingBadge(Long uid, WearingBadgeReq req);

    /**
     * 徽章聚合信息-返回的代表需要刷新的
     * @param req
     * @return
     */
    List<ItemInfoDTO> getItemInfo(ItemInfoReq req);

}
