package com.neryn.norman.service.Impl;

import com.neryn.norman.NormanMethods;
import com.neryn.norman.repository.GlobalProfileRepository;
import com.neryn.norman.service.GlobalProfileService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.neryn.norman.entity.GlobalProfile;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GlobalProfileServiceImpl implements GlobalProfileService {

    private final NormanMethods normanMethods;
    private final GlobalProfileRepository repository;

    public GlobalProfile findById(Long userId) {
        return repository.findById(userId).orElse(null);
    }
    public GlobalProfile findByUsername(String username) {
        return repository.findByUsername(username).orElse(null);
    }

    public GlobalProfile save(GlobalProfile profile) {
        return repository.save(profile);
    }
    public void saveAll(List<GlobalProfile> profiles) {
        repository.saveAll(profiles);
    }

    public String getNickname(@NonNull GlobalProfile profile, boolean link, boolean emoji) {
        String nickname, linkStr;
        if(profile.getNickname() != null) nickname = profile.getNickname();
        else if(profile.getTgName() != null) nickname = profile.getTgName();
        else nickname = profile.getId().toString();

        if(emoji) {
            if(profile.getRightEmoji() != null) nickname = nickname + " " + profile.getRightEmoji().getEmoji();
            if(profile.getLeftEmoji() != null) nickname = profile.getLeftEmoji().getEmoji() + " " + nickname;
        }

        if(link) {
            linkStr = (profile.getUsername() != null) ?
                    "t.me/" + profile.getUsername() :
                    "tg://openmessage?user_id=" + profile.getId();
            return String.format("<a href=\"%s\">%s</a>", linkStr, nickname);
        } else return nickname;
    }

    public GlobalProfile updateProfile(User user) {
        GlobalProfile profile = findById(user.getId());
        String tgName = normanMethods.clearString(user.getFirstName(), false);
        if(tgName.isBlank()) tgName = user.getId().toString();

        if(profile == null)
            profile = repository.save(new GlobalProfile(user.getId(), user.getUserName(), tgName));

        else {
            boolean update = false;
            if(user.getUserName() != null && (profile.getUsername() == null || !profile.getUsername().equals(user.getUserName()))) {
                GlobalProfile oldUNOwner = findByUsername(user.getUserName());
                if(oldUNOwner != null) {
                    oldUNOwner.setUsername(null);
                    save(oldUNOwner);
                }

                profile.setUsername(user.getUserName());
                update = true;
            }

            else if(user.getUserName() == null && profile.getUsername() != null) {
                profile.setUsername(null);
                update = true;
            }

            if(profile.getTgName() == null || !tgName.equals(profile.getTgName())) {
                profile.setTgName(tgName);
                update = true;
            }

            if(update) repository.save(profile);
        } return profile;
    }
    public void updateDiamondsLimit() {
        repository.updateDiamondsLimit();
    }
}
