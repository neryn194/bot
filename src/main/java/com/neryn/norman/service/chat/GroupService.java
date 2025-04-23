package com.neryn.norman.service.chat;

import com.neryn.norman.entity.chat.ChatGroup;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface GroupService {

    ChatGroup findById(Long id);
    List<ChatGroup> findAll();
    List<ChatGroup> findAllByFamilyId(Long familyId);
    List<ChatGroup> findTopDonation(int limit);
    ChatGroup save(ChatGroup group);

    String getGroupName(ChatGroup group);
    ChatGroup updateTgInfo(ChatGroup group, String name, String link);

    enum AdminPerm {
        RESTRICT_MEMBERS
    }
}
