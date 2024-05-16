package com.ywt.common.event;


import com.ywt.chat.domain.entity.GroupMember;
import com.ywt.chat.domain.entity.RoomGroup;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class GroupMemberAddEvent extends ApplicationEvent {

    private final List<GroupMember> memberList;
    private final RoomGroup roomGroup;
    private final Long inviteUid;

    public GroupMemberAddEvent(Object source, RoomGroup roomGroup, List<GroupMember> memberList, Long inviteUid) {
        super(source);
        this.memberList = memberList;
        this.roomGroup = roomGroup;
        this.inviteUid = inviteUid;
    }

}
