package com.neryn.norman.entity.chat;

import com.neryn.norman.enums.Command;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Entity
@NoArgsConstructor
public class AccessToChat {

    @EmbeddedId
    private AtcPK id;
    private int lvl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", insertable = false, updatable = false)
    private ChatGroup group;

    @Embeddable
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AtcPK implements Serializable {

        @Column(name = "chat_id")
        private Long chatId;
        private Command access;
    }

    public AccessToChat(Long chatId, Command access, int level) {
        this.id = new AtcPK(chatId, access);
        this.lvl = level;
    }
}
