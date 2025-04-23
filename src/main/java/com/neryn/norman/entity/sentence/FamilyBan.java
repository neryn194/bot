package com.neryn.norman.entity.sentence;

import com.neryn.norman.entity.GlobalProfile;
import com.neryn.norman.entity.chat.Family;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
public class FamilyBan {

    @EmbeddedId
    private FamilyBanPK id;

    @Column(name = "moder_id")
    private Long moderId;
    private LocalDateTime time;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Family family;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private GlobalProfile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moder_id", referencedColumnName = "id", insertable = false, updatable = false)
    private GlobalProfile moder;


    public FamilyBan(Long userId, Long familyId, Long moderId, LocalDateTime time, String description) {
        this.id = new FamilyBanPK(userId, familyId);
        this.moderId = moderId;
        this.time = time;
        this.description = description;
    }

    @Data
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FamilyBanPK {

        @Column(name = "user_id")
        private Long userId;

        @Column(name = "family_id")
        private Long familyId;
    }
}
