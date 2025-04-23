package com.neryn.norman.service.sentence;

import com.neryn.norman.entity.sentence.FamilyBan;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FamilyBanService {

    FamilyBan findByUserIdAndFamilyId(Long userId, Long familyId);
    List<FamilyBan> findPageByFamilyId(Long familyId, int limit, int page);
    List<FamilyBan> findAllExpiredBan();

    void save(FamilyBan ban);
    void delete(FamilyBan ban);
    void deleteAll(List<FamilyBan> bans);
}
