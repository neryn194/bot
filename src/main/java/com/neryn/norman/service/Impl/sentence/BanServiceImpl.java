package com.neryn.norman.service.Impl.sentence;

import com.neryn.norman.entity.GroupProfile;
import com.neryn.norman.entity.sentence.Ban;
import com.neryn.norman.repository.sentence.BanRepository;
import com.neryn.norman.service.sentence.BanService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class BanServiceImpl implements BanService {

    private final BanRepository repository;

    public Ban findById(Long userId, Long chatId) {
        return repository.findById(new GroupProfile.GroupProfilePK(userId, chatId)).orElse(null);
    }
    public List<Ban> findPageByChatId(Long chatId, int limit, int page) {
        return repository.findPageByChatId(chatId, limit + 1, (page-1) * limit);
    }
    public List<Ban> findAllExpiredBan() {
        return repository.findAllExpiredBan();
    }

    public void save(Ban ban) {
        repository.save(ban);
    }
    public void delete(Ban ban) {
        repository.delete(ban);
    }
    public void saveAll(List<Ban> bans) {
        repository.saveAll(bans);
    }
    public void deleteAll(List<Ban> bans) {
        repository.deleteAllInBatch(bans);
    }
}