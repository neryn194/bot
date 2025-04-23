package com.neryn.norman.service;

import com.neryn.norman.entity.Robbery;
import org.springframework.stereotype.Service;

@Service
public interface RobberyService {

    Robbery findById(Long chatId, Long leaderId);

    void save(Robbery robbery);
    void delete(Robbery robbery);
}
