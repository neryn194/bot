package com.neryn.norman.repository.chat;

import com.neryn.norman.entity.chat.ChatGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<ChatGroup, Long> {

    @Query(value = "SELECT chat FROM ChatGroup chat " +
            "WHERE chat.rating != 0 " +
            "ORDER BY chat.rating DESC " +
            "LIMIT :limit")
    List<ChatGroup> findTopDonation(int limit);

    @Query(value = "SELECT chat FROM ChatGroup chat " +
            "WHERE chat.familyId = :familyId " +
            "ORDER BY chat.familyId ASC")
    List<ChatGroup> findAllByFamilyId(Long familyId);
}
