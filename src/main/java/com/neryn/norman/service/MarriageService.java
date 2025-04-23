package com.neryn.norman.service;

import com.neryn.norman.entity.Marriage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MarriageService {

    Marriage findById(Long chatId, Long firstUserId, Long secondUserId);
    Marriage findUserMarriage(Long chatId, Long userId);
    List<Marriage> findTopPage(int limit, int page);
    List<Marriage> findPageByChatId(Long chatId, int limit, int page);
    List<Marriage> findAllNotConfirmed(Long chatId, Long userId);

    void save(Marriage marriage);
    void delete(Marriage marriage);
    void deleteAll(List<Marriage> marriages);
}
