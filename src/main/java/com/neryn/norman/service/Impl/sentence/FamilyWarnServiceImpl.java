package com.neryn.norman.service.Impl.sentence;

import com.neryn.norman.entity.sentence.FamilyWarn;
import com.neryn.norman.repository.sentence.FamilyWarnRepository;
import com.neryn.norman.service.sentence.FamilyWarnService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FamilyWarnServiceImpl implements FamilyWarnService {

    private final FamilyWarnRepository repository;

    public List<FamilyWarn> findAllByUserIdAndFamilyId(Long userId, Long familyId) {
        return repository.findAllByUserIdAndFamilyId(userId, familyId);
    }
    public List<FamilyWarn> findPageByFamilyId(Long familyId, int limit, int page) {
        return repository.findPageByFamilyId(familyId, limit + 1, (page-1) * limit);
    }
    public List<FamilyWarn> findAllExpiredWarns() {
        return repository.findAllExpiredWarns();
    }

    public void save(FamilyWarn ban) {
        repository.save(ban);
    }
    public void delete(FamilyWarn ban) {
        repository.delete(ban);
    }
    public void deleteAll(List<FamilyWarn> warns) {
        repository.deleteAll(warns);
    }
}
