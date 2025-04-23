package com.neryn.norman.service.Impl;

import com.neryn.norman.entity.Robbery;
import com.neryn.norman.repository.RobberyRepository;
import com.neryn.norman.service.RobberyService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RobberyServiceImpl implements RobberyService {

    private final RobberyRepository repository;

    public Robbery findById(Long chatId, Long leaderId) {
        return repository.findById(new Robbery.RobberyPK(leaderId, chatId)).orElse(null);
    }

    public void save(Robbery robbery) {
        repository.save(robbery);
    }
    public void delete(Robbery robbery) {
        repository.deleteById(robbery.getId());
    }
}
