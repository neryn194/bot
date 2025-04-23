package com.neryn.norman.service.Impl.clan;

import com.neryn.norman.entity.clan.estate.ClanCamp;
import com.neryn.norman.entity.clan.estate.ClanEstateAbs;
import com.neryn.norman.entity.clan.estate.ClanMine;
import com.neryn.norman.entity.clan.estate.ClanSmithy;
import com.neryn.norman.repository.clan.ClanCampRepository;
import com.neryn.norman.repository.clan.ClanMineRepository;
import com.neryn.norman.repository.clan.ClanSmithyRepository;
import com.neryn.norman.service.clan.ClanEstateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClanEstateServiceImpl implements ClanEstateService {

    private final ClanCampRepository campRepository;
    private final ClanMineRepository mineRepository;
    private final ClanSmithyRepository smithyRepository;

    public ClanCamp findCampById(Long chatId, Integer clanId, Integer number) {
        return campRepository.findById(new ClanEstateAbs.ClanEstatePK(chatId, clanId, number)).orElse(null);
    }
    public ClanMine findMineById(Long chatId, Integer clanId, Integer number) {
        return mineRepository.findById(new ClanEstateAbs.ClanEstatePK(chatId, clanId, number)).orElse(null);
    }
    public ClanSmithy findSmithyById(Long chatId, Integer clanId, Integer number) {
        return smithyRepository.findById(new ClanEstateAbs.ClanEstatePK(chatId, clanId, number)).orElse(null);
    }

    public void saveAllCamps(List<ClanCamp> camps) {
        campRepository.saveAll(camps);
    }
    public void saveAllMines(List<ClanMine> mines) {
        mineRepository.saveAll(mines);
    }
    public void saveAllSmithies(List<ClanSmithy> smithies) {
        smithyRepository.saveAll(smithies);
    }

    public void save(ClanEstateAbs estate) {
        if(estate instanceof ClanCamp camp) campRepository.save(camp);
        else if(estate instanceof ClanMine mine) mineRepository.save(mine);
        else if(estate instanceof ClanSmithy smithy) smithyRepository.save(smithy);
    }
}
