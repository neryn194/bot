package com.neryn.norman.repository;

import com.neryn.norman.entity.GroupProfile;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupProfileRepository extends JpaRepository<GroupProfile, GroupProfile.GroupProfilePK> {

    @Query(value = "SELECT profile FROM GroupProfile profile " +
            "WHERE profile.id.chatId = :chatId AND profile.nickname IS NOT NULL " +
            "ORDER BY profile.post ASC, profile.nickname ASC " +
            "LIMIT :limit OFFSET :offset")
    List<GroupProfile> findAllNicknames(Long chatId, int limit, int offset);

    @Query(value = "SELECT profile FROM GroupProfile profile " +
            "WHERE profile.id.chatId = :chatId AND profile.moder = :moder")
    List<GroupProfile> findAllModers(Long chatId, int moder);

    @Query(value = "SELECT profile FROM GroupProfile profile " +
            "WHERE profile.id.chatId = :chatId AND profile.clanId = :clanId " +
            "ORDER BY profile.clanPost ASC")
    List<GroupProfile> findAllByClan(Long chatId, Integer clanId);

    @Query(value = "SELECT profile FROM GroupProfile profile " +
            "WHERE profile.id.chatId = :chatId AND profile.robberyLeaderId = :leaderId")
    List<GroupProfile> findAllByRobberyLeaderId(Long chatId, Long leaderId);


    @Query(value = "SELECT profile FROM GroupProfile profile " +
            "WHERE profile.id.chatId = :chatId AND profile.statDay > 0 " +
            "ORDER BY profile.statDay DESC " +
            "LIMIT :limit")
    List<GroupProfile> findTopStatDay(Long chatId, int limit);

    @Query(value = "SELECT profile FROM GroupProfile profile " +
            "WHERE profile.id.chatId = :chatId AND profile.statWeek > 0 " +
            "ORDER BY profile.statWeek DESC " +
            "LIMIT :limit")
    List<GroupProfile> findTopStatWeek(Long chatId, int limit);

    @Query(value = "SELECT profile FROM GroupProfile profile " +
            "WHERE profile.id.chatId = :chatId AND profile.statMonth > 0 " +
            "ORDER BY profile.statMonth DESC " +
            "LIMIT :limit")
    List<GroupProfile> findTopStatMonth(Long chatId, int limit);

    @Query(value = "SELECT profile FROM GroupProfile profile " +
            "WHERE profile.id.chatId = :chatId AND profile.statTotal > 0 " +
            "ORDER BY profile.statTotal DESC " +
            "LIMIT :limit")
    List<GroupProfile> findTopStatTotal(Long chatId, int limit);


    @Modifying
    @Transactional
    @Query(value = "UPDATE GroupProfile SET rouletteTime = null")
    void updateRouletteTime();

    @Modifying
    @Transactional
    @Query(value = "UPDATE GroupProfile SET statDay = 0")
    void updateStatDay();

    @Modifying
    @Transactional
    @Query(value = "UPDATE GroupProfile SET statWeek = 0")
    void updateStatWeek();

    @Modifying
    @Transactional
    @Query(value = "UPDATE GroupProfile SET statMonth = 0")
    void updateStatMonth();

    @Modifying
    @Transactional
    @Query(value = "UPDATE GroupProfile SET statTotal = 0")
    void updateStatTotal();
}
