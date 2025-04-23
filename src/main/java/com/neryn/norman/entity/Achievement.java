package com.neryn.norman.entity;

import com.neryn.norman.enums.AchievementEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Entity
@NoArgsConstructor
public class Achievement {

    @EmbeddedId
    private AchievementPK id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private GlobalProfile profile;

    public Achievement(Long userId, AchievementEnum achievement) {
        this.id = new AchievementPK(userId, achievement);
    }

    @Data
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AchievementPK implements Serializable {

        @Column(name = "user_id")
        private Long userId;

        private AchievementEnum achievement;
    }
}
