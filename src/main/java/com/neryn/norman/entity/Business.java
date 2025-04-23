package com.neryn.norman.entity;

import com.neryn.norman.enums.Item;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class Business implements EmojiCarrier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nominative;
    private String genitive;
    private Item rightEmoji;
    private Item leftEmoji;
    private String name;
    private boolean hasName;

    @Column(name = "owner_id")
    private Long ownerId;
    private int price;

    private int diamonds;

    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id", insertable = false, updatable = false)
    private GlobalProfile owner;

    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Company company;

    public String getName() {
        String name;
        if(this.name == null) {
            name = this.nominative;
            if (rightEmoji != null) name = name + " " + rightEmoji.getEmoji();
            if (leftEmoji != null) name = leftEmoji.getEmoji() + " " + name;
        }

        else {
            name = this.name;
            if (rightEmoji != null) name = name + " " + rightEmoji.getEmoji();
            if (leftEmoji != null) name = leftEmoji.getEmoji() + " " + name;
            name = String.format("%s\n«%s»", this.nominative, name);
        }

        return name;
    }
}
