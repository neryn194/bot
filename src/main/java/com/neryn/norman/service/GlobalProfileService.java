package com.neryn.norman.service;

import com.neryn.norman.entity.GlobalProfile;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;

@Service
public interface GlobalProfileService {

    GlobalProfile findById(Long userId);
    GlobalProfile findByUsername(String username);
    GlobalProfile save(GlobalProfile profile);
    void saveAll(List<GlobalProfile> profiles);

    String getNickname(GlobalProfile profile, boolean link, boolean emoji);
    GlobalProfile updateProfile(User user);
    void updateDiamondsLimit();
}
