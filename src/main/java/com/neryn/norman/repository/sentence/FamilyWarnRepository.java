package com.neryn.norman.repository.sentence;

import com.neryn.norman.entity.sentence.FamilyWarn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FamilyWarnRepository extends JpaRepository<FamilyWarn, Long> {

    @Query(value = "SELECT warn FROM FamilyWarn warn " +
            "WHERE warn.userId = :userId AND warn.familyId = :familyId")
    List<FamilyWarn> findAllByUserIdAndFamilyId(Long userId, Long familyId);

    @Query(
            nativeQuery = true,
            value = "SELECT * FROM family_warn " +
                    "WHERE family_id = :familyId " +
                    "LIMIT :limit OFFSET :offset"
    )
    List<FamilyWarn> findPageByFamilyId(Long familyId, int limit, int offset);

    @Query(value = "SELECT warn FROM FamilyWarn warn WHERE warn.time <= CURRENT_TIMESTAMP")
    List<FamilyWarn> findAllExpiredWarns();
}
