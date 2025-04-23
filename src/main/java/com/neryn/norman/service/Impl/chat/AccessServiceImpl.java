package com.neryn.norman.service.Impl.chat;

import com.neryn.norman.entity.chat.AccessToChat;
import com.neryn.norman.service.chat.AccessService;
import com.neryn.norman.enums.Command;
import com.neryn.norman.repository.chat.AccessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccessServiceImpl implements AccessService {

    private final AccessRepository repository;

    public int findById(Long chatId, Command access) {
        AccessToChat.AtcPK id = new AccessToChat.AtcPK(chatId, access);
        AccessToChat atc = repository.findById(id).orElse(null);

        if(atc != null) return atc.getLvl();
        else {
            atc = new AccessToChat(chatId, access, access.getDefaultModerLevel());
            save(atc);
            return access.getDefaultModerLevel();
        }
    }
    public List<AccessToChat> findAllByChatId(Long chatId) {
        return repository.findAllByChatId(chatId);
    }

    public void save(AccessToChat atc) {
        repository.save(atc);
    }
    public List<AccessToChat> saveAll(List<AccessToChat> atcs) {
        return repository.saveAll(atcs);
    }
}
