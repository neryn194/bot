package com.neryn.norman.service.Impl.chat;

import com.neryn.norman.commands.FamilyCommands;
import com.neryn.norman.entity.chat.Family;
import com.neryn.norman.entity.chat.FamilyModer;
import com.neryn.norman.repository.chat.FamilyModerRepository;
import com.neryn.norman.repository.chat.FamilyRepository;
import com.neryn.norman.service.chat.FamilyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FamilyServiceImpl implements FamilyService {

    private final FamilyRepository repository;
    private final FamilyModerRepository moderRepository;

    public Family findById(Long id) {
        return repository.findById(id).orElse(null);
    }
    public List<Family> findAllByLeaderId(Long leaderId) {
        return repository.findAllByLeaderId(leaderId);
    }
    public Family save(Family family) {
        return repository.save(family);
    }
    public void delete(Family family) {
        repository.delete(family);
    }

    public FamilyModer findModerById(Long userId, Long familyId) {
        return moderRepository.findById(new FamilyModer.ModerPK(userId, familyId)).orElse(null);
    }
    public List<FamilyModer> findAllFamilyModers(Long familyId, FamilyCommands.ModerRank level) {
        return moderRepository.findAllFamilyModers(familyId, level);
    }
    public void saveModer(FamilyModer moder) {
        moderRepository.save(moder);
    }
    public void deleteModer(FamilyModer moder) {
        moderRepository.delete(moder);
    }
}
