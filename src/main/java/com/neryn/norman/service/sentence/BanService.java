package com.neryn.norman.service.sentence;

import com.neryn.norman.entity.sentence.Ban;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BanService {

    Ban findById(Long userId, Long chatId);
    List<Ban> findPageByChatId(Long chatId, int limit, int page);
    List<Ban> findAllExpiredBan();

    void save(Ban ban);
    void delete(Ban ban);
    void saveAll(List<Ban> bans);
    void deleteAll(List<Ban> bans);
}
