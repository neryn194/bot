package com.neryn.norman.entity.clan;

import com.neryn.norman.entity.GroupProfile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Entity
@NoArgsConstructor
public class ClanInvite {

    @EmbeddedId
    private InvitePK id;

    @Column(name = "member_id")
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "chat_id", referencedColumnName = "chat_id", insertable = false, updatable = false),
            @JoinColumn(name = "clan_id", referencedColumnName = "clan_id", insertable = false, updatable = false)
    })
    private Clan clan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "chat_id", referencedColumnName = "chat_id", insertable = false, updatable = false),
            @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    })
    private GroupProfile userProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "chat_id", referencedColumnName = "chat_id", insertable = false, updatable = false),
            @JoinColumn(name = "member_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    })
    private GroupProfile memberProfile;


    @Data
    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InvitePK implements Serializable {

        @Column(name = "chat_id")
        private Long chatId;

        @Column(name = "clan_id")
        private Integer clanId;

        @Column(name = "user_id")
        private Long userId;

    }

    public ClanInvite(Long chatId, Integer clanId, Long userId, Long memberId) {

        this.id = new InvitePK(chatId, clanId, userId);
        this.memberId = memberId;
    }
}
