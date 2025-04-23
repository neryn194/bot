package com.neryn.norman.service.Impl.sentence;

import com.neryn.norman.entity.sentence.Warn;
import com.neryn.norman.repository.sentence.WarnRepository;
import com.neryn.norman.service.sentence.WarnService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class WarnServiceImpl implements WarnService {

    private final WarnRepository repository;

    public Warn findById(Long id) {
        return repository.findById(id).orElse(null);
    }
    public List<Warn> findPageByChatId(Long chatId, int limit, int page) {
        return repository.findPageByChatId(chatId, limit + 1, (page-1) * limit);
    }
    public List<Warn> findAllByUserIdAndChatId(Long userId, Long chatId) {
        return repository.findAllByUserIdAndChatId(userId, chatId);
    }
    public List<Warn> findAllExpiredWarn() {
        return repository.findAllExpiredWarn();
    }

    public void save(Warn warn) {
        repository.save(warn);
    }
    public void delete(Warn warn) {
        repository.deleteById(warn.getId());
    }
    public void saveAll(List<Warn> warns) {
        repository.saveAll(warns);
    }
    public void deleteAll(List<Warn> warns) {
        repository.deleteAllInBatch(warns);
    }
}
