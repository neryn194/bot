package com.neryn.norman.repository.sentence;

import com.neryn.norman.entity.GroupProfile;
import com.neryn.norman.entity.sentence.Ban;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BanRepository extends JpaRepository<Ban, GroupProfile.GroupProfilePK> {

    @Query(
            nativeQuery = true,
            value = "SELECT * FROM ban " +
                    "WHERE chat_id = :chatId " +
                    "ORDER BY time ASC " +
                    "LIMIT :limit OFFSET :offset"
    )
    List<Ban> findPageByChatId(Long chatId, int limit, int offset);

    @Query(value = "SELECT ban FROM Ban ban " +
            "WHERE ban.time <= CURRENT_TIMESTAMP")
    List<Ban> findAllExpiredBan();
}
