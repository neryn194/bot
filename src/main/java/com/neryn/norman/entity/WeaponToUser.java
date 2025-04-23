package com.neryn.norman.entity;

import com.neryn.norman.commands.WeaponCommands;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Entity
@NoArgsConstructor
public class WeaponToUser {

    @EmbeddedId
    private WeaponToUserPK id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false),
            @JoinColumn(name = "chat_id", referencedColumnName = "chat_id", insertable = false, updatable = false)
    })
    private GroupProfile profile;

    public WeaponToUser(Long chatId, Long userId, WeaponCommands.Weapon weapon) {
        this.id = new WeaponToUserPK(chatId, userId, weapon);
    }

    @Data
    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WeaponToUserPK implements Serializable {

        @Column(name = "chat_id")
        private Long chatId;

        @Column(name = "user_id")
        private Long userId;

        @Column(name = "weapon_id")
        private WeaponCommands.Weapon weapon;
    }
}
