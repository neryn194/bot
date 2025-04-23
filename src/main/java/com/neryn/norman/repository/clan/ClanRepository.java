package com.neryn.norman.repository.clan;

import com.neryn.norman.entity.clan.Clan;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClanRepository extends JpaRepository<Clan, Clan.ClanPK> {

    @Query(value = "SELECT clan FROM Clan clan " +
            "WHERE clan.id.chatId = :chatId " +
            "ORDER BY clan.id.clanId ASC")
    List<Clan> findAllByChatId(Long chatId);

    @Query(nativeQuery = true,
            value = "SELECT * FROM clan " +
            "WHERE chat_id = :chatId " +
            "ORDER BY clan_id ASC " +
            "LIMIT :limit OFFSET :offset")
    List<Clan> findPageByChatId(Long chatId, int limit, int offset);

    @Query(value = "SELECT clan FROM Clan clan " +
            "WHERE clan.rating != 0 " +
            "ORDER BY clan.rating DESC " +
            "LIMIT :limit")
    List<Clan> findTopClans(int limit);

    @Query(value = "SELECT clan FROM Clan clan " +
            "WHERE clan.maxRating != 0 " +
            "ORDER BY clan.maxRating DESC " +
            "LIMIT :limit")
    List<Clan> findTopClansMax(int limit);

    @Query(value = "SELECT clan FROM Clan clan " +
            "WHERE clan.totalRating != 0 " +
            "ORDER BY clan.totalRating DESC " +
            "LIMIT :limit")
    List<Clan> findTopClansTotal(int limit);

    @Modifying
    @Transactional
    @Query(value = "UPDATE Clan " +
            "SET totalRating = totalRating + rating/100, rating = 0 " +
            "WHERE rating > 0")
    void resetSeason();
}
