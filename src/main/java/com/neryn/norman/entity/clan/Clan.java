package com.neryn.norman.entity.clan;

import com.neryn.norman.commands.ClanCommands;
import com.neryn.norman.entity.EmojiCarrier;
import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.entity.GroupProfile;
import com.neryn.norman.entity.clan.estate.ClanCamp;
import com.neryn.norman.entity.clan.estate.ClanMine;
import com.neryn.norman.entity.clan.estate.ClanSmithy;
import com.neryn.norman.enums.Item;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
public class Clan implements EmojiCarrier {

    @EmbeddedId
    private ClanPK id;
    private String name;
    private Item rightEmoji;
    private Item leftEmoji;
    private String description;

    @Column(name = "leader_id")
    private Long leaderId;
    private ClanType type;

    private int maxMembers;
    private int level;
    private int experience;
    private int rating;
    private int maxRating;
    private int totalRating;

    private int diamonds;
    private int coins;
    private int ore;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", insertable = false, updatable = false)
    private ChatGroup group;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "leader_id", referencedColumnName = "user_id", insertable = false, updatable = false),
            @JoinColumn(name = "chat_id", referencedColumnName = "chat_id", insertable = false, updatable = false)
    })
    private GroupProfile leader;

    @OneToOne(mappedBy = "clan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ClanRaid raid;

    @OneToMany(mappedBy = "clan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClanCamp> camps;

    @OneToMany(mappedBy = "clan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClanMine> mines;

    @OneToMany(mappedBy = "clan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClanSmithy> smithies;

    @OneToMany(mappedBy = "clan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClanInvite> invites;

    @OneToMany(mappedBy = "clan", fetch = FetchType.LAZY)
    private List<GroupProfile> members;


    @Data
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClanPK implements Serializable {

        @Column(name = "chat_id")
        private Long chatId;

        @Column(name = "clan_id")
        private Integer clanId;
    }

    public Clan(Long chatId, Integer clanId, String name, Long leaderId) {
        this.id = new ClanPK(chatId, clanId);
        this.name = name;
        this.leaderId = leaderId;
        this.type = ClanType.OPEN;
        this.maxMembers = ClanCommands.START_LIMIT_CLAN_MEMBERS;
        this.level = 1;
        this.experience = 0;
        this.ore = 0;
        this.rating = maxRating = totalRating = 0;
    }

    public String getName() {
        String name = this.name;
        if(rightEmoji != null) name = name + " " + rightEmoji.getEmoji();
        if(leftEmoji != null) name = leftEmoji.getEmoji() + " " + name;
        return name;
    }

    public String getNameWithoutEmoji() {
        return name;
    }


    @Getter
    @AllArgsConstructor
    public enum ClanType {

        OPEN("открытый"),
        INVITE_ONLY("по приглашениям"),
        CLOSED("закрытый");

        private final String name;
    }

    @Getter
    @AllArgsConstructor
    public enum ClanMemberPost {
        LEADER(3, "глава"),
        CO_LEADER(2, "соруководитель"),
        ELDER(1, "старейшина"),
        MEMBER(0, "участник");

        private final int level;
        private final String name;
    }
}
