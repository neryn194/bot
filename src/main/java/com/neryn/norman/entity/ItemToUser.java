package com.neryn.norman.entity;

import com.neryn.norman.enums.Item;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Entity
@NoArgsConstructor
public class ItemToUser {

    @EmbeddedId
    private ItemToUserPK id;
    private int count;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private GlobalProfile profile;

    @Data
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemToUserPK implements Serializable {

        @Column(name = "user_id")
        private Long userId;
        private Item item;
    }

    public ItemToUser(Long userId, Item item) {
        this.id = new ItemToUserPK(userId, item);
        this.count = 1;
    }

    public ItemToUser(Long userId, Item item, int count) {
        this.id = new ItemToUserPK(userId, item);
        this.count = count;
    }
}
