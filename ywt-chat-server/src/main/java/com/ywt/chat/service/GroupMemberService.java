package com.ywt.chat.service;

import com.ywt.chat.domain.vo.Req.member.AdminAddReq;
import com.ywt.chat.domain.vo.Req.member.AdminRevokeReq;
import com.ywt.chat.domain.vo.Req.member.MemberExitReq;

import java.util.List;

/**
 * <p>
 * 群成员表 服务类
 * </p>
 *
 * @author ywt
 * @since 2024-05-02
 */
public interface GroupMemberService {

    /**
     * 群成员
     * @param groupId
     * @return
     */
    List<Long> getGroupMembers(long groupId);

    /**
     * 用户退出群聊
     * @param uid
     * @param request
     */
    void exitGroup(Long uid, MemberExitReq request);

    /**
     * 添加管理员
     * @param uid
     * @param request
     */
    void addAdmin(Long uid, AdminAddReq request);

    /**
     * 撤销管理员
     * @param uid
     * @param request
     */
    void revokeAdmin(Long uid, AdminRevokeReq request);
}
