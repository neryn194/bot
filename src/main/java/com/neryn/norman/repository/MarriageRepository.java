package com.neryn.norman.repository;

import com.neryn.norman.entity.Marriage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarriageRepository extends JpaRepository<Marriage, Marriage.MarriagePK> {

    @Query(
            nativeQuery = true,
            value = "SELECT * FROM marriage " +
                    "WHERE confirmed = true " +
                    "ORDER BY level DESC, experience DESC " +
                    "LIMIT :limit OFFSET :offset"
    )
    List<Marriage> findTopPage(int limit, int offset);

    @Query(
            nativeQuery = true,
            value = "SELECT * FROM marriage " +
                    "WHERE chat_id = :chatId AND confirmed = true " +
                    "ORDER BY level DESC, experience DESC " +
                    "LIMIT :limit OFFSET :offset"
    )
    List<Marriage> findPageByChatId(Long chatId, int limit, int offset);

    @Query(value = "SELECT marriage FROM Marriage marriage " +
            "WHERE marriage.id.chatId = :chatId " +
            "AND (marriage.id.firstUserId = :userId OR marriage.id.secondUserId = :userId) " +
            "AND marriage.confirmed = false")
    List<Marriage> findAllNotConfirmed(Long chatId, Long userId);

    @Query(value = "SELECT marriage FROM Marriage marriage " +
            "WHERE marriage.id.chatId = :chatId " +
            "AND (marriage.id.firstUserId = :userId OR marriage.id.secondUserId = :userId) " +
            "AND marriage.confirmed = true")
    Optional<Marriage> findUserMarriage(Long chatId, Long userId);
}
