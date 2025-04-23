package com.neryn.norman.repository.clan;

import com.neryn.norman.entity.clan.ClanInvite;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ClanInviteRepository extends JpaRepository<ClanInvite, ClanInvite.InvitePK> {

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM ClanInvite WHERE id.chatId = :chatId AND id.userId = :userId")
    void removeUserInvites(Long chatId, Long userId);
}