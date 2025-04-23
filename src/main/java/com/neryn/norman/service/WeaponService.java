package com.neryn.norman.service;

import com.neryn.norman.commands.WeaponCommands;
import com.neryn.norman.entity.WeaponToUser;
import org.springframework.stereotype.Service;

@Service
public interface WeaponService {

    WeaponToUser findById(Long chatId, Long userId, WeaponCommands.Weapon weapon);
    void save(WeaponToUser weapon);
    void delete(WeaponToUser weapon);
}
