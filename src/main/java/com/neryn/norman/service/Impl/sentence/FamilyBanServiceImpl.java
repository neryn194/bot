package com.neryn.norman.service.Impl.sentence;

import com.neryn.norman.entity.sentence.FamilyBan;
import com.neryn.norman.repository.sentence.FamilyBanRepository;
import com.neryn.norman.service.sentence.FamilyBanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FamilyBanServiceImpl implements FamilyBanService {

    private final FamilyBanRepository repository;

    public FamilyBan findByUserIdAndFamilyId(Long userId, Long familyId) {
        return repository.findByUserIdAndFamilyId(userId, familyId);
    }
    public List<FamilyBan> findPageByFamilyId(Long familyId, int limit, int page) {
        return repository.findPageByFamilyId(familyId, limit + 1, (page-1) * limit);
    }
    public List<FamilyBan> findAllExpiredBan() {
        return repository.findAllExpiredBan();
    }

    public void save(FamilyBan ban) {
        repository.save(ban);
    }
    public void delete(FamilyBan ban) {
        repository.delete(ban);
    }
    public void deleteAll(List<FamilyBan> bans) {
        repository.deleteAllInBatch(bans);
    }
}
