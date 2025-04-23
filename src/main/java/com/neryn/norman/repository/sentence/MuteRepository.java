package com.neryn.norman.repository.sentence;

import com.neryn.norman.entity.GroupProfile;
import com.neryn.norman.entity.sentence.Mute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MuteRepository extends JpaRepository<Mute, GroupProfile.GroupProfilePK> {

    @Query(
            nativeQuery = true,
            value = "SELECT * FROM mute " +
                    "WHERE chat_id = :chatId " +
                    "ORDER BY time ASC " +
                    "LIMIT :limit OFFSET :offset"
    )
    List<Mute> findPageByChatId(Long chatId, int limit, int offset);

    @Query(value = "SELECT mute FROM Mute mute " +
            "WHERE mute.time <= CURRENT_TIMESTAMP")
    List<Mute> findAllExpiredMute();
}
