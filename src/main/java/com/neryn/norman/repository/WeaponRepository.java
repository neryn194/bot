package com.neryn.norman.repository;

import com.neryn.norman.entity.WeaponToUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeaponRepository extends JpaRepository<WeaponToUser, WeaponToUser.WeaponToUserPK> {
}
