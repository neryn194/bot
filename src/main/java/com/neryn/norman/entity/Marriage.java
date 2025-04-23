package com.neryn.norman.entity;

import com.neryn.norman.commands.MarriageCommands;
import com.neryn.norman.entity.chat.ChatGroup;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class Marriage {

    @EmbeddedId
    private MarriagePK id;
    private LocalDateTime date;
    private MarriageCommands.MarriageLevel level;
    private int experience;
    private LocalDateTime meetingTime;
    private MarriageCommands.MeetingPlace meetingPlace;
    private boolean confirmed;


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

    public Marriage(Long chatId, Long firstUserId, Long secondUserId) {
        this.id = new MarriagePK(chatId, firstUserId, secondUserId);
        this.confirmed = false;
        this.experience = 0;
    }


    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarriagePK implements Serializable {

        @Column(name = "chat_id")
        private Long chatId;

        @Column(name = "first_user_id")
        private Long firstUserId;

        @Column(name = "second_user_id")
        private Long secondUserId;
    }
}

