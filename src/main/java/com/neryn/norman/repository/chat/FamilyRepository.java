package com.neryn.norman.repository.chat;

import com.neryn.norman.entity.chat.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FamilyRepository extends JpaRepository<Family, Long> {

    @Query(value = "SELECT family FROM Family family " +
            "WHERE family.leaderId = :leaderId " +
            "ORDER BY family.id ASC")
    List<Family> findAllByLeaderId(Long leaderId);
}
