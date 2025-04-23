package com.neryn.norman.service.sentence;

import com.neryn.norman.entity.sentence.FamilyWarn;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FamilyWarnService {
    List<FamilyWarn> findAllByUserIdAndFamilyId(Long userId, Long familyId);
    List<FamilyWarn> findPageByFamilyId(Long familyId, int limit, int page);
    List<FamilyWarn> findAllExpiredWarns();

    void save(FamilyWarn ban);
    void delete(FamilyWarn ban);
    void deleteAll(List<FamilyWarn> warns);
}
