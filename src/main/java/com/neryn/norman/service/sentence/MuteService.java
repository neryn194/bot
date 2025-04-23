package com.neryn.norman.service.sentence;

import com.neryn.norman.entity.sentence.Mute;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MuteService {

    Mute findById(Long userId, Long chatId);
    List<Mute> findPageByChatId(Long chatId, int limit, int page);
    List<Mute> findAllExpiredMute();

    void save(Mute mute);
    void delete(Mute mute);
    void saveAll(List<Mute> mutes);
    void deleteAll(List<Mute> mutes);
}
