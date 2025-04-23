package com.neryn.norman.entity.chat;

import com.neryn.norman.enums.ChatAchievementEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Entity
@NoArgsConstructor
public class ChatAchievement {

    @EmbeddedId
    private ChatAchievementPK id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", referencedColumnName = "id", insertable = false, updatable = false)
    private ChatGroup group;

    @Data
    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatAchievementPK implements Serializable {

        @Column(name = "chat_id")
        private Long chatId;
        private ChatAchievementEnum achievement;
    }

    public ChatAchievement(Long chatId, ChatAchievementEnum achievement) {
        this.id = new ChatAchievementPK(chatId, achievement);
    }
}
