package com.neryn.norman.service.Impl;

import com.neryn.norman.entity.Duel;
import com.neryn.norman.repository.DuelRepository;
import com.neryn.norman.service.DuelService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class DuelServiceImpl implements DuelService {

    private final DuelRepository repository;

    public Duel findById(Long chatId, Long firstUserId, Long secondUserId) {
        Duel.DuelPK firstId = new Duel.DuelPK(chatId, firstUserId, secondUserId);
        Duel.DuelPK secondId = new Duel.DuelPK(chatId, secondUserId, firstUserId);
        return repository.findById(firstId).orElse(repository.findById(secondId).orElse(null));
    }
    public Duel findStartedChallenge(Long chatId, Long userId) {
        return repository.findStartedChallenges(chatId, userId).orElse(null);
    }

    public List<Duel> findAllNotStartedChallenges(Long chatId, Long userId) {
        return repository.findAllNotStartedChallenges(chatId, userId);
    }

    public void save(Duel duel) {
        repository.save(duel);
    }
    public void delete(Duel duel) {
        repository.deleteById(duel.getId());
    }
    public void deleteAll(List<Duel> duels) {
        repository.deleteAllInBatch(duels);
    }
}
