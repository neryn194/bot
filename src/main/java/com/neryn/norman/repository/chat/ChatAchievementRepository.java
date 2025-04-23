package com.neryn.norman.repository.chat;

import com.neryn.norman.entity.chat.ChatAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatAchievementRepository extends JpaRepository<ChatAchievement, ChatAchievement.ChatAchievementPK> {}
