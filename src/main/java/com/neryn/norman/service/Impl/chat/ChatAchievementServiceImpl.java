package com.neryn.norman.service.Impl.chat;

import com.neryn.norman.entity.chat.ChatAchievement;
import com.neryn.norman.enums.ChatAchievementEnum;
import com.neryn.norman.repository.chat.ChatAchievementRepository;
import com.neryn.norman.service.chat.ChatAchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatAchievementServiceImpl implements ChatAchievementService {

    private final ChatAchievementRepository repository;

    public ChatAchievement findById(Long chatId, ChatAchievementEnum achievement) {
        return repository.findById(new ChatAchievement.ChatAchievementPK(chatId, achievement)).orElse(null);
    }

    public void save(ChatAchievement achievement) {
        repository.save(achievement);
    }
    public void delete(ChatAchievement achievement) {
        repository.delete(achievement);
    }
}
