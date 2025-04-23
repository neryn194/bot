package com.neryn.norman.service;

import com.neryn.norman.entity.Achievement;
import com.neryn.norman.enums.AchievementEnum;
import org.springframework.stereotype.Service;

@Service
public interface AchievementService {
    Achievement findById(Long userId, AchievementEnum achievement);
    void save(Achievement achievement);
    void delete(Achievement achievement);
}
