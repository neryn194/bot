package com.neryn.norman.repository;

import com.neryn.norman.entity.Robbery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RobberyRepository extends JpaRepository<Robbery, Robbery.RobberyPK> {}
