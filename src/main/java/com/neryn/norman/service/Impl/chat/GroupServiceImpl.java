package com.neryn.norman.service.Impl.chat;

import com.neryn.norman.NormanMethods;
import com.neryn.norman.repository.chat.GroupRepository;
import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.service.chat.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final NormanMethods normanMethods;
    private final GroupRepository repository;

    public ChatGroup findById(Long id) {
        return repository.findById(id).orElse(null);
    }
    public List<ChatGroup> findAll() {
        return repository.findAll();
    }
    public List<ChatGroup> findAllByFamilyId(Long familyId) {
        return repository.findAllByFamilyId(familyId);
    }
    public List<ChatGroup> findTopDonation(int limit) {
        return repository.findTopDonation(limit);
    }
    public ChatGroup save(ChatGroup group) {
        return repository.save(group);
    }

    public String getGroupName(ChatGroup group) {
        if(group != null && group.getName() != null) return group.getName();
        else if(group.getTgName() != null) return group.getTgName();
        else return null;
    }
    public ChatGroup updateTgInfo(ChatGroup group, String name, String link) {
        name = normanMethods.clearString(name, false);
        if(name.isBlank()) name = group.getId().toString();

        if((group.getTgName() == null || !group.getTgName().equals(name)) ||
                (((group.getTgLink() == null || !group.getTgLink().equals(link)) && link != null)) ||
                (group.getTgLink() != null && link == null)) {

            group.setTgLink(link);
            group.setTgName(name);
            repository.save(group);
        } return group;
    }
}
