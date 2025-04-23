package com.neryn.norman.entity;

import com.neryn.norman.commands.ActivityCommands;
import com.neryn.norman.commands.ClanRaidCommands;
import com.neryn.norman.commands.WeaponCommands;
import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.entity.clan.Clan;
import com.neryn.norman.entity.clan.ClanRaid;
import com.neryn.norman.entity.sentence.Ban;
import com.neryn.norman.entity.sentence.Mute;
import com.neryn.norman.entity.sentence.Warn;
import com.neryn.norman.enums.Specialization;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
public class GroupProfile {

    @EmbeddedId
    private GroupProfilePK id;
    private String nickname;
    private String post;
    private String description;
    private LocalDateTime onset;
    private int moder;
    private int coins;
    private LocalTime rouletteTime;
    private boolean isHome;

    private int statDay;
    private int statWeek;
    private int statMonth;
    private int statTotal;

    private int crossbowBolts;
    private int diamondRings;
    private int jewelry;
    private int wine;


    @Column(name = "robbery_leader_id")
    private Long robberyLeaderId;
    private LocalDateTime robberyBreak;

    private ActivityCommands.Job job;
    private LocalDateTime jobTime;

    @Column(name = "clan_id")
    private Integer clanId;
    private LocalDateTime farmTime;

    @Column(name = "raid_id")
    private Integer raidId;
    private Clan.ClanMemberPost clanPost;
    private Specialization firstSpecialization;
    private Specialization secondSpecialization;
    private int firstSpecializationLevel;
    private int secondSpecializationLevel;
    private WeaponCommands.Weapon weapon;

    private LocalDateTime trainingTime;
    private ClanRaidCommands.TrainingType trainingType;
    private Specialization trainingSpecialization;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private GlobalProfile globalProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", insertable = false, updatable = false)
    private ChatGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "chat_id", referencedColumnName = "chat_id", insertable = false, updatable = false),
            @JoinColumn(name = "robbery_leader_id", referencedColumnName = "leader_id", insertable = false, updatable = false),
    })
    private Robbery robbery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "chat_id", referencedColumnName = "chat_id", insertable = false, updatable = false),
            @JoinColumn(name = "raid_id", referencedColumnName = "clan_id", insertable = false, updatable = false),
    })
    private ClanRaid raid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "chat_id", referencedColumnName = "chat_id", insertable = false, updatable = false),
            @JoinColumn(name = "clan_id", referencedColumnName = "clan_id", insertable = false, updatable = false),
    })
    private Clan clan;

    @OneToOne(mappedBy = "profile", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Ban ban;

    @OneToOne(mappedBy = "profile", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Mute mute;

    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Warn> warns;

    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<WeaponToUser> weapons;


    public Long getChatId() {
        return this.getId().getChatId();
    }

    public Long getUserId() {
        return this.getId().getUserId();
    }

    @Data
    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GroupProfilePK implements Serializable {

        @Column(name = "user_id")
        private Long userId;

        @Column(name = "chat_id")
        private Long chatId;
    }

    public GroupProfile(Long userId, Long chatId) {
        this.id = new GroupProfilePK(userId, chatId);
        this.onset = LocalDateTime.now();
        this.moder = 0;
        this.coins = 0;
        this.isHome = false;

        this.statDay = this.statWeek = this.statMonth = this.statTotal = 0;
        this.crossbowBolts = this.diamondRings = jewelry = wine = 0;
        this.firstSpecializationLevel = this.secondSpecializationLevel = 1;

    }
}
