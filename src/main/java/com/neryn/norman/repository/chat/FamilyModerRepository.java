package com.neryn.norman.repository.chat;

import com.neryn.norman.commands.FamilyCommands;
import com.neryn.norman.entity.chat.FamilyModer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FamilyModerRepository extends JpaRepository<FamilyModer, FamilyModer.ModerPK> {

    @Query(value = "SELECT moder FROM FamilyModer moder " +
            "WHERE moder.id.familyId = :familyId AND moder.rank = :rank")
    List<FamilyModer> findAllFamilyModers(Long familyId, FamilyCommands.ModerRank rank);
}
