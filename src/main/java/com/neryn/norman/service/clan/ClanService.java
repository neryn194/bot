package com.neryn.norman.service.clan;

import com.neryn.norman.entity.clan.Clan;
import com.neryn.norman.entity.clan.ClanInvite;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ClanService {
    Clan findById(Long chatId, Integer clanId);
    List<Clan> findTopClans(int limit);
    List<Clan> findTopClansMax(int limit);
    List<Clan> findTopClansTotal(int limit);
    List<Clan> findAllByChatId(Long chatId);
    List<Clan> findPageByChatId(Long chatId, int limit, int page);

    Clan save(Clan clan);
    void delete(Clan clan);
    void resetSeason();

    // Invites
    ClanInvite findInviteById(Long chatId, Integer clanId, Long userId);
    void saveInvite(ClanInvite invite);
    void deleteInvite(ClanInvite invite);
    void removeUserInvites(Long chatId, Long userId);
}
