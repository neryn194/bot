package com.neryn.norman.service;

import com.neryn.norman.entity.Duel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DuelService {

    Duel findById(Long chatId, Long firstUserId, Long secondUserId);

    Duel findStartedChallenge(Long chatId, Long userId);
    List<Duel> findAllNotStartedChallenges(Long chatId, Long userId);

    void save(Duel duel);
    void delete(Duel duel);
    void deleteAll(List<Duel> duels);
}
