package com.neryn.norman.service.Impl;

import com.neryn.norman.commands.WeaponCommands;
import com.neryn.norman.entity.WeaponToUser;
import com.neryn.norman.repository.WeaponRepository;
import com.neryn.norman.service.WeaponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeaponServiceImpl implements WeaponService {

    private final WeaponRepository repository;

    public WeaponToUser findById(Long chatId, Long userId, WeaponCommands.Weapon weapon) {
        return repository.findById(new WeaponToUser.WeaponToUserPK(chatId, userId, weapon)).orElse(null);
    }

    public void save(WeaponToUser weapon) {
        repository.save(weapon);
    }

    public void delete(WeaponToUser weapon) {
        repository.delete(weapon);
    }
}
