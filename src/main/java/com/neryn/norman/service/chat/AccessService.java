package com.neryn.norman.service.chat;

import com.neryn.norman.entity.chat.AccessToChat;
import com.neryn.norman.enums.Command;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AccessService {

    int findById(Long chatId, Command access);
    List<AccessToChat> findAllByChatId(Long chatId);

    void save(AccessToChat atc);
    List<AccessToChat> saveAll(List<AccessToChat> atc);
}
