package com.neryn.norman.service.Impl;

import com.neryn.norman.entity.Achievement;
import com.neryn.norman.enums.AchievementEnum;
import com.neryn.norman.repository.AchievementRepository;
import com.neryn.norman.service.AchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AchievementServiceImpl implements AchievementService {

    private final AchievementRepository repository;

    public Achievement findById(Long userId, AchievementEnum achievement) {
        return repository.findById(new Achievement.AchievementPK(userId, achievement)).orElse(null);
    }

    public void save(Achievement achievement) {
        repository.save(achievement);
    }
    public void delete(Achievement achievement) {
        repository.delete(achievement);
    }
}
