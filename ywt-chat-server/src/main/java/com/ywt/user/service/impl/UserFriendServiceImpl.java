package com.ywt.user.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.ywt.chat.dao.RoomFriendDao;
import com.ywt.chat.domain.entity.RoomFriend;
import com.ywt.chat.domain.enums.MessageTypeEnum;
import com.ywt.chat.domain.vo.Req.ChatMessageReq;
import com.ywt.chat.service.ChatService;
import com.ywt.chat.service.RoomService;
import com.ywt.chat.service.adapter.MessageAdapter;
import com.ywt.common.annotation.RedissonLock;
import com.ywt.common.domain.vo.Req.CursorPageBaseReq;
import com.ywt.common.domain.vo.Req.PageBaseReq;
import com.ywt.common.domain.vo.Resp.CursorPageBaseResp;
import com.ywt.common.domain.vo.Resp.PageBaseResp;
import com.ywt.common.event.UserApplyEvent;
import com.ywt.common.utils.AssertUtil;
import com.ywt.user.dao.UserApplyDao;
import com.ywt.user.dao.UserDao;
import com.ywt.user.dao.UserFriendDao;
import com.ywt.user.domain.entity.User;
import com.ywt.user.domain.entity.UserApply;
import com.ywt.user.domain.entity.UserFriend;
import com.ywt.user.domain.enums.ApplyStatusEnum;
import com.ywt.user.domain.enums.ChatActiveStatusEnum;
import com.ywt.user.domain.vo.Req.friend.FriendApplyReq;
import com.ywt.user.domain.vo.Req.friend.FriendApproveReq;
import com.ywt.user.domain.vo.Req.friend.FriendCheckReq;
import com.ywt.user.domain.vo.Req.friend.FriendDeleteReq;
import com.ywt.user.domain.vo.Resp.friend.FriendApplyResp;
import com.ywt.user.domain.vo.Resp.friend.FriendCheckResp;
import com.ywt.user.domain.vo.Resp.friend.FriendResp;
import com.ywt.user.domain.vo.Resp.friend.FriendUnreadResp;
import com.ywt.user.service.UserFriendService;
import com.ywt.user.service.adapter.FriendAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.ywt.user.domain.enums.ApplyStatusEnum.WAIT_APPROVAL;

/**
 * 功能描述
 *
 * @author: ywt
 * @date: 2024年04月30日 15:55
 */
@Service
@Slf4j
public class UserFriendServiceImpl implements UserFriendService {

    @Autowired
    private UserFriendDao friendDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserApplyDao userApplyDao;

    @Autowired
    @Lazy
    private UserFriendService friendService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private RoomService roomService;

    @Autowired
    private ChatService chatService;



    @Override
    public CursorPageBaseResp<FriendResp> friendList(Long uid, CursorPageBaseReq cursorPageBaseReq) {
        CursorPageBaseResp<UserFriend> friendPage =  friendDao.getFriendPage(uid, cursorPageBaseReq);
        if (friendPage.isEmpty()) {
            return CursorPageBaseResp.empty();
        }
        List<Long> friendUids = friendPage.getList().stream().map(UserFriend::getFriendUid).collect(Collectors.toList());
        List<User> userList = userDao.getFriendList(friendUids);
        return new CursorPageBaseResp<>(friendPage.getCursor(),friendPage.getIsLast(), FriendAdapter.buildFriend(friendPage.getList(), userList));
    }
    /**
     * 检查
     * 检查是否是自己好友
     *
     */
    @Override
    public FriendCheckResp check(Long uid, FriendCheckReq checkReq) {
        List<UserFriend> friendList = friendDao.getFriendList(uid,checkReq.getUidList());
        Set<Long> friendUidSet = friendList.stream().map(UserFriend::getFriendUid).collect(Collectors.toSet());
        List<FriendCheckResp.FriendCheck> friendCheckList = checkReq.getUidList().stream().map(id -> {
            FriendCheckResp.FriendCheck friendCheck = new FriendCheckResp.FriendCheck();
            friendCheck.setUid(id);
            friendCheck.setIsFriend(friendUidSet.contains(id));
            return friendCheck;
        }).collect(Collectors.toList());

        return new FriendCheckResp(friendCheckList);
    }

    /**
     * 申请好友
     * @param uid
     * @param request
     */
    @Override
    @RedissonLock(key = "#uid")
    public void apply(Long uid, FriendApplyReq request) {
        //是否有好友关系
        UserFriend friend = friendDao.getFriend(uid,request.getTargetUid());
        AssertUtil.isEmpty(friend,"你们已经是好友了");
        // 查看是否有未审核的申请(自己的)
        UserApply userApply = userApplyDao.getFriendApproving(uid,request.getTargetUid());
        if (ObjectUtil.isNotNull(userApply)) {
            log.info("已有好友申请记录,uid:{}, targetId:{}", uid, request.getTargetUid());
            return;
        }
        //是否有待审批的申请记录(别人请求自己的)
        UserApply friendApply = userApplyDao.getFriendApproving(request.getTargetUid(),uid);
        if (ObjectUtil.isNotNull(friendApply)) {
            friendService.applyApprove(uid,new FriendApproveReq(friendApply.getId()));
        }
        // 入库
        UserApply insert = FriendAdapter.buildUserApply(uid,request);
        userApplyDao.save(insert);
        // 申请事件
        applicationEventPublisher.publishEvent(new UserApplyEvent(this,insert));
    }

    @Override
//    @Transactional(rollbackFor = Exception.class)
    @RedissonLock(key = "#uid")
    public void applyApprove(Long uid, FriendApproveReq friendApproveReq) {
        UserApply userApply = userApplyDao.getById(friendApproveReq.getApplyId());
        AssertUtil.isNotEmpty(userApply,"不存在申请记录");
        AssertUtil.equal(userApply.getTargetId(), uid, "不存在申请记录");
        AssertUtil.equal(userApply.getStatus(), WAIT_APPROVAL.getCode(), "已同意好友申请");
        // 同意申请
        userApplyDao.agree(friendApproveReq.getApplyId());
        // 创建好友关系
        createFriend(uid,userApply.getUid());
        // 创建聊天室
        RoomFriend roomFriend = roomService.createRoomFriend(Arrays.asList(uid, userApply.getUid()));
        // 发送第一条信息
        chatService.sendMsg(MessageAdapter.buildAgreeMsg(roomFriend.getRoomId()),uid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFriend(Long uid, Long friendUid) {
        List<UserFriend> userFriends = friendDao.getUserFriend(uid,friendUid);
        if (CollectionUtil.isEmpty(userFriends)) {
            log.info("没有好友关系：{},{}", uid, friendUid);
            return;
        }
        List<Long> friendRecordIds = userFriends.stream().map(UserFriend::getId).collect(Collectors.toList());
        friendDao.removeByIds(friendRecordIds);
        // 禁用房间
        roomService.disableChat(Arrays.asList(uid,friendUid));
        // 删除好友申请列表
        userApplyDao.deleteFriendApply(uid);

    }


    @Override
    public FriendUnreadResp unread(Long uid) {
        return new FriendUnreadResp(userApplyDao.getUnReadCount(uid));
    }

    @Override
    public PageBaseResp<FriendApplyResp> getPageApplyFriend(Long uid, PageBaseReq request) {
        Page page = request.plusPage();
        IPage<UserApply> userApplyIPage = userApplyDao.getApplyPage(uid,page);
        if (CollectionUtil.isEmpty(userApplyIPage.getRecords())) {
            return PageBaseResp.empty();
        }
        //将这些申请列表设为已读
        readApples(uid,userApplyIPage);
        //返回消息
        return PageBaseResp.init(userApplyIPage, FriendAdapter.buildFriendApplyList(userApplyIPage.getRecords()));
    }

    private void readApples(Long uid, IPage<UserApply> userApplyIPage) {
        List<Long> applyIds = userApplyIPage.getRecords().stream().map(UserApply::getId).collect(Collectors.toList());
        userApplyDao.readApples(uid, applyIds);
    }

    private void createFriend(Long uid, Long friendUid) {
        UserFriend userFriend1= new UserFriend();
        userFriend1.setUid(uid);
        userFriend1.setFriendUid(friendUid);
        UserFriend userFriend2= new UserFriend();
        userFriend2.setUid(friendUid);
        userFriend2.setFriendUid(uid);
        friendDao.saveBatch(Lists.newArrayList(userFriend1,userFriend2));
    }
}
