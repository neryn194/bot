package com.neryn.norman.service.Impl.clan;

import com.neryn.norman.entity.clan.Clan;
import com.neryn.norman.entity.clan.ClanInvite;
import com.neryn.norman.repository.clan.ClanInviteRepository;
import com.neryn.norman.repository.clan.ClanRepository;
import com.neryn.norman.service.clan.ClanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClanServiceImpl implements ClanService {

    private final ClanRepository repository;
    private final ClanInviteRepository inviteRepository;

    public Clan findById(Long chatId, Integer clanId) {
        Clan.ClanPK id = new Clan.ClanPK(chatId, clanId);
        return repository.findById(id).orElse(null);
    }
    public List<Clan> findAllByChatId(Long chatId) {
        return repository.findAllByChatId(chatId);
    }
    public List<Clan> findPageByChatId(Long chatId, int limit, int page) {
        return repository.findPageByChatId(chatId, limit + 1, (page-1) * limit);
    }
    public List<Clan> findTopClans(int limit) {
        return repository.findTopClans(limit);
    }
    public List<Clan> findTopClansMax(int limit) {
        return repository.findTopClansMax(limit);
    }
    public List<Clan> findTopClansTotal(int limit) {
        return repository.findTopClansTotal(limit);
    }

    public Clan save(Clan clan) {
        return repository.save(clan);
    }
    public void delete(Clan clan) {
        repository.deleteById(clan.getId());
    }
    public void resetSeason() {
        repository.resetSeason();
    }

    // invites

    public ClanInvite findInviteById(Long chatId, Integer clanId, Long userId) {
        ClanInvite.InvitePK id = new ClanInvite.InvitePK(chatId, clanId, userId);
        return inviteRepository.findById(id).orElse(null);
    }
    public void saveInvite(ClanInvite invite) {
        inviteRepository.save(invite);
    }
    public void deleteInvite(ClanInvite invite) {
        inviteRepository.delete(invite);
    }
    public void removeUserInvites(Long chatId, Long userId) {
        inviteRepository.removeUserInvites(chatId, userId);
    }
}
