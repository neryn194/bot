package com.neryn.norman.service.Impl.clan;

import com.neryn.norman.entity.clan.Clan;
import com.neryn.norman.entity.clan.ClanRaid;
import com.neryn.norman.repository.clan.ClanRaidRepository;
import com.neryn.norman.service.clan.ClanRaidService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClanRaidServiceImpl implements ClanRaidService {

    private final ClanRaidRepository repository;

    public ClanRaid findById(Long chatId, Integer clanId) {
        return repository.findById(new Clan.ClanPK(chatId, clanId)).orElse(null);
    }
    public void save(ClanRaid raid) {
        repository.save(raid);
    }

    public void delete(ClanRaid raid) {
        repository.delete(raid);
    }
}
