package com.neryn.norman.service.Impl.sentence;

import com.neryn.norman.entity.GroupProfile;
import com.neryn.norman.entity.sentence.Mute;
import com.neryn.norman.repository.sentence.MuteRepository;
import com.neryn.norman.service.sentence.MuteService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class MuteServiceImpl implements MuteService {

    private final MuteRepository repository;

    public Mute findById(Long userId, Long chatId) {
        return repository.findById(
                new GroupProfile.GroupProfilePK(userId, chatId)
        ).orElse(null);
    }
    public List<Mute> findPageByChatId(Long chatId, int limit, int page) {
        return repository.findPageByChatId(chatId, limit + 1, (page-1) * limit);
    }
    public List<Mute> findAllExpiredMute() {
        return repository.findAllExpiredMute();
    }

    public void save(Mute mute) {
        repository.save(mute);
    }
    public void delete(Mute mute) {
        repository.delete(mute);
    }
    public void saveAll(List<Mute> mutes) {
        repository.saveAll(mutes);
    }
    public void deleteAll(List<Mute> mutes) {
        repository.deleteAllInBatch(mutes);
    }
}
