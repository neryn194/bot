package com.neryn.norman.repository.chat;

import com.neryn.norman.entity.chat.AccessToChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessRepository extends JpaRepository<AccessToChat, AccessToChat.AtcPK> {

    @Query(value = "SELECT atc FROM AccessToChat atc " +
            "WHERE atc.id.chatId = :chatId " +
            "ORDER BY atc.id.access ASC")
    List<AccessToChat> findAllByChatId(Long chatId);
}
