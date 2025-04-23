package com.neryn.norman.service.chat;

import com.neryn.norman.entity.chat.ChatAchievement;
import com.neryn.norman.enums.ChatAchievementEnum;
import org.springframework.stereotype.Service;

@Service
public interface ChatAchievementService {
    ChatAchievement findById(Long chatId, ChatAchievementEnum achievement);
    void save(ChatAchievement achievement);
    void delete(ChatAchievement achievement);
}
