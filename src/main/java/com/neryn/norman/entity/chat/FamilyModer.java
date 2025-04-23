package com.neryn.norman.entity.chat;

import com.neryn.norman.commands.FamilyCommands;
import com.neryn.norman.entity.GlobalProfile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class FamilyModer {

    @EmbeddedId
    private ModerPK id;
    private FamilyCommands.ModerRank rank;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private GlobalProfile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Family family;

    public FamilyModer(Long userId, Long familyId, FamilyCommands.ModerRank rank) {
        this.id = new ModerPK(userId, familyId);
        this.rank = rank;
    }

    @Data
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModerPK {

        @Column(name = "user_id")
        private Long userId;

        @Column(name = "family_id")
        private Long familyId;
    }
}
