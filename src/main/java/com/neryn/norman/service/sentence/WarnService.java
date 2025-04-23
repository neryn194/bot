package com.neryn.norman.service.sentence;

import com.neryn.norman.entity.sentence.Warn;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface WarnService {

    Warn findById(Long id);
    List<Warn> findPageByChatId(Long chatId, int limit, int page);
    List<Warn> findAllByUserIdAndChatId(Long userId, Long chatId);
    List<Warn> findAllExpiredWarn();

    void save(Warn warn);
    void delete(Warn warn);
    void saveAll(List<Warn> warns);
    void deleteAll(List<Warn> warns);
}
