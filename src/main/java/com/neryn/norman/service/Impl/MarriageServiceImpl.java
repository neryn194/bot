package com.neryn.norman.service.Impl;

import com.neryn.norman.entity.Marriage;
import com.neryn.norman.repository.MarriageRepository;
import com.neryn.norman.service.MarriageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarriageServiceImpl implements MarriageService {

    private final MarriageRepository repository;

    public Marriage findById(Long chatId, Long firstUserId, Long secondUserId) {
        Marriage.MarriagePK firstId = new Marriage.MarriagePK(chatId, firstUserId, secondUserId);
        Marriage.MarriagePK secondId = new Marriage.MarriagePK(chatId, secondUserId, firstUserId);
        return repository.findById(firstId).orElse(repository.findById(secondId).orElse(null));
    }
    public Marriage findUserMarriage(Long chatId, Long userId) {
        return repository.findUserMarriage(chatId, userId).orElse(null);
    }
    public List<Marriage> findTopPage(int limit, int page) {
        return repository.findTopPage(limit + 1, (page - 1) * limit);
    }
    public List<Marriage> findPageByChatId(Long chatId, int limit, int page) {
        return repository.findPageByChatId(chatId, limit + 1, (page-1) * limit);
    }
    public List<Marriage> findAllNotConfirmed(Long chatId, Long userId) {
        return repository.findAllNotConfirmed(chatId, userId);
    }

    public void save(Marriage marriage) {
        repository.save(marriage);
    }
    public void delete(Marriage marriage) {
        repository.deleteById(marriage.getId());
    }
    public void deleteAll(List<Marriage> marriages) {
        repository.deleteAllInBatch(marriages);
    }
}
