package com.neryn.norman.service.chat;

import com.neryn.norman.commands.FamilyCommands;
import com.neryn.norman.entity.chat.Family;
import com.neryn.norman.entity.chat.FamilyModer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FamilyService {
    Family findById(Long id);
    List<Family> findAllByLeaderId(Long leaderId);
    Family save(Family family);
    void delete(Family family);

    // Moders
    FamilyModer findModerById(Long userId, Long familyId);
    List<FamilyModer> findAllFamilyModers(Long familyId, FamilyCommands.ModerRank level);
    void saveModer(FamilyModer moder);
    void deleteModer(FamilyModer moder);
}
