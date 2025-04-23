package com.neryn.norman.entity;

import com.neryn.norman.commands.RobberyCommands;
import com.neryn.norman.entity.chat.ChatGroup;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
public class Robbery {

    @EmbeddedId
    private RobberyPK id;
    private RobberyCommands.RobberyLocation location;
    private boolean started;
    private LocalDateTime finishTime;
    private boolean masks;
    private boolean guns;
    private boolean drill;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", insertable = false, updatable = false)
    private ChatGroup group;

    @OneToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumns({
            @PrimaryKeyJoinColumn(name = "leader_id", referencedColumnName = "user_id"),
            @PrimaryKeyJoinColumn(name = "chat_id", referencedColumnName = "chat_id")
    })
    private GroupProfile leader;

    @OneToMany(mappedBy = "robbery", fetch = FetchType.LAZY)
    private List<GroupProfile> members;


    @Data
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RobberyPK implements Serializable {

        @Column(name = "leader_id")
        private Long leaderId;

        @Column(name = "chat_id")
        private Long chatId;
    }

    public Robbery(Long leaderId, Long chatId) {
        this.id = new RobberyPK(leaderId, chatId);
        this.started = false;
        this.masks = this.guns = this.drill = false;
    }
}
