package com.neryn.norman.repository;

import com.neryn.norman.entity.Duel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DuelRepository extends JpaRepository<Duel, Duel.DuelPK> {

    @Query(value = "SELECT duel FROM Duel duel " +
            "WHERE (duel.id.firstUserId = :userId OR duel.id.secondUserId = :userId) " +
            "AND duel.id.chatId = :chatId AND duel.started = true " +
            "ORDER BY time DESC " +
            "LIMIT 1"
    )
    Optional<Duel> findStartedChallenges(Long chatId, Long userId);

    @Query(value = "SELECT duel FROM Duel duel " +
            "WHERE (duel.id.firstUserId = :userId OR duel.id.secondUserId = :userId) " +
            "AND duel.id.chatId = :chatId AND duel.started = false"
    )
    List<Duel> findAllNotStartedChallenges(Long chatId, Long userId);
}
