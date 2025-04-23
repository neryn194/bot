package com.neryn.norman.entity;

import com.neryn.norman.entity.chat.Family;
import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.entity.sentence.FamilyBan;
import com.neryn.norman.entity.sentence.FamilyWarn;
import com.neryn.norman.enums.AchievementEnum;
import com.neryn.norman.enums.Item;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
public class GlobalProfile implements EmojiCarrier {

    @Id
    private Long id;
    private String username;
    private String tgName;
    private String nickname;
    private Item rightEmoji;
    private Item leftEmoji;
    private String description;
    private AchievementEnum favAchievement;
    private boolean hidden;

    @Column(name = "home_chat_id")
    private Long homeChatId;
    private LocalDateTime onset;

    private int stars;
    private int normanCoins;
    private int diamonds;
    private int diamondsLimit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_chat_id", insertable = false, updatable = false)
    private ChatGroup homeChat;

    @OneToOne(mappedBy = "owner", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Company company;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Business> businesses;

    @OneToMany(mappedBy = "globalProfile", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<GroupProfile> groupProfiles;

    @OneToMany(mappedBy = "leader", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Family> families;

    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<FamilyBan> familyBans;

    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<FamilyWarn> familyWarns;

    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Achievement> achievements;

    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ItemToUser> emojies;

    public GlobalProfile(Long id, String username, String tgName) {
        this.id = id;
        this.username = username;
        this.tgName = tgName;
        this.hidden = true;
        this.homeChatId = null;
        this.onset = LocalDateTime.now();
        this.stars = this.normanCoins = this.diamonds = 0;
    }
}
