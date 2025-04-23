package com.neryn.norman.service.clan;

import com.neryn.norman.entity.clan.estate.ClanCamp;
import com.neryn.norman.entity.clan.estate.ClanEstateAbs;
import com.neryn.norman.entity.clan.estate.ClanMine;
import com.neryn.norman.entity.clan.estate.ClanSmithy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ClanEstateService {

    ClanCamp findCampById(Long chatId, Integer clanId, Integer number);
    ClanMine findMineById(Long chatId, Integer clanId, Integer number);
    ClanSmithy findSmithyById(Long chatId, Integer clanId, Integer number);

    void saveAllCamps(List<ClanCamp> camps);
    void saveAllMines(List<ClanMine> mines);
    void saveAllSmithies(List<ClanSmithy> smithies);
    
    void save(ClanEstateAbs estate);
}
