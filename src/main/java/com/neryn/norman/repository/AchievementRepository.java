package com.neryn.norman.repository;

import com.neryn.norman.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Achievement.AchievementPK> {}
