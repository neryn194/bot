package com.neryn.norman.entity;

import com.neryn.norman.entity.chat.ChatGroup;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
public class Duel {

    @EmbeddedId
    private DuelPK id;
    private Integer firstUserAim;
    private Integer secondUserAim;
    private LocalDateTime time;
    private Integer coins;
    private Integer messageId;
    private boolean started;
    private boolean firstPlayerMove;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", insertable = false, updatable = false)
    private ChatGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "first_user_id", referencedColumnName = "user_id", insertable = false, updatable = false),
            @JoinColumn(name = "chat_id", referencedColumnName = "chat_id", insertable = false, updatable = false)
    })
    private GroupProfile firstProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "second_user_id", referencedColumnName = "user_id", insertable = false, updatable = false),
            @JoinColumn(name = "chat_id", referencedColumnName = "chat_id", insertable = false, updatable = false)
    })
    private GroupProfile secondProfile;


    @Data
    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DuelPK implements Serializable {

        @Column(name = "chat_id")
        private Long chatId;

        @Column(name = "first_user_id")
        private Long firstUserId;

        @Column(name = "second_user_id")
        private Long secondUserId;
    }

    public Duel(Long chatId, Long firstUserId, Long secondUserId, int minutes) {
        this.id = new DuelPK(chatId, firstUserId, secondUserId);
        this.firstUserAim = this.secondUserAim = 3;
        this.time = LocalDateTime.now().plusMinutes(minutes);
        this.started = false;
        this.firstPlayerMove = true;
    }
}
