package com.neryn.norman.service.clan;

import com.neryn.norman.entity.clan.ClanRaid;
import org.springframework.stereotype.Service;

@Service
public interface ClanRaidService {

    ClanRaid findById(Long chatId, Integer clanId);
    void save(ClanRaid raid);
    void delete(ClanRaid raid);
}
