package com.neryn.norman.repository.sentence;

import com.neryn.norman.entity.sentence.Warn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarnRepository extends JpaRepository<Warn, Long> {

    @Query(
            nativeQuery = true,
            value = "SELECT * FROM warn " +
                    "WHERE chat_id = :chatId " +
                    "ORDER BY time ASC " +
                    "LIMIT :limit OFFSET :offset"
    )
    List<Warn> findPageByChatId(Long chatId, int limit, int offset);

    @Query(value = "SELECT warn FROM Warn warn " +
            "WHERE warn.time <= CURRENT_TIMESTAMP")
    List<Warn> findAllExpiredWarn();

    List<Warn> findAllByUserIdAndChatId(Long userId, Long chatId);
}
