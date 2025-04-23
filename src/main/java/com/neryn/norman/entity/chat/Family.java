package com.neryn.norman.entity.chat;

import com.neryn.norman.entity.GlobalProfile;
import com.neryn.norman.entity.sentence.FamilyBan;
import com.neryn.norman.entity.sentence.FamilyWarn;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@NoArgsConstructor
public class Family {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "leader_id")
    private Long leaderId;
    private String name;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", referencedColumnName = "id", insertable = false, updatable = false)
    private GlobalProfile leader;

    @OneToMany(mappedBy = "family", fetch = FetchType.LAZY)
    private List<ChatGroup> groups;

    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FamilyModer> moders;

    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FamilyBan> bans;

    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FamilyWarn> warns;


    public Family(Long leaderId, String name) {
        this.leaderId = leaderId;
        this.name = name;
    }
}
