package com.neryn.norman.repository.sentence;

import com.neryn.norman.entity.sentence.FamilyBan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FamilyBanRepository extends JpaRepository<FamilyBan, FamilyBan.FamilyBanPK> {

    @Query(value = "SELECT ban FROM FamilyBan ban " +
            "WHERE ban.id.userId = :userId AND ban.id.familyId = :familyId")
    FamilyBan findByUserIdAndFamilyId(Long userId, Long familyId);

    @Query(
            nativeQuery = true,
            value = "SELECT * FROM family_ban " +
                    "WHERE family_id = :familyId " +
                    "LIMIT :limit OFFSET :offset"
    )
    List<FamilyBan> findPageByFamilyId(Long familyId, int limit, int offset);

    @Query(value = "SELECT ban FROM FamilyBan ban WHERE ban.time <= CURRENT_TIMESTAMP")
    List<FamilyBan> findAllExpiredBan();
}
