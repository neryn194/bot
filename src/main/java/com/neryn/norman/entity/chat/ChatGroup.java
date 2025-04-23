package com.neryn.norman.entity.chat;

import com.neryn.norman.commands.WeaponCommands;
import com.neryn.norman.entity.Duel;
import com.neryn.norman.entity.GroupProfile;
import com.neryn.norman.entity.Marriage;
import com.neryn.norman.entity.clan.Clan;
import com.neryn.norman.entity.sentence.Ban;
import com.neryn.norman.entity.sentence.Mute;
import com.neryn.norman.entity.sentence.Warn;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
public class ChatGroup {

    @Id
    private Long id;
    private String name;
    private String description;
    private String tgName;
    private String tgLink;
    private String greeting;
    private boolean blocked;

    private int stat;
    private LocalDateTime statTime;
    private int warnLimit;
    private int rouletteInterval;
    private boolean onSentenceCommands;

    private LocalDateTime premium;
    private int rating;
    private WeaponCommands.WorkshopLevel workshopLevel;
    private LocalDateTime workshopUpTime;

    @Column(name = "family_id")
    private Long familyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Family family;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GroupProfile> profiles;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AccessToChat> access;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Clan> clans;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Marriage> marriages;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Duel> duels;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ban> bans;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Mute> mutes;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Warn> warns;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatAchievement> achievements;

    public ChatGroup(Long chatId) {
        this.id = chatId;
        this.onSentenceCommands = false;
        this.blocked = false;
        this.stat = 0;
        this.warnLimit = 3;
        this.rouletteInterval = 0;
        this.rating = 0;
        this.workshopLevel = WeaponCommands.WorkshopLevel.L0;
    }
}
