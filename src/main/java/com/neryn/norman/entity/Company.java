package com.neryn.norman.entity;

import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.enums.Item;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

import static com.neryn.norman.commands.BusinessCommands.*;

@Data
@Entity
@NoArgsConstructor
public class Company implements EmojiCarrier {

    @Id
    private Long id;
    private String name;
    private Item rightEmoji;
    private Item leftEmoji;
    private String description;
    private int normanCoins;
    private int reputation;

    @Column(name = "headquarters_id")
    private Long headquartersId;
    private LocalDate collectingDate;

    private AccountingLevel accounting;
    private SecuritiesLevel securities;
    private ProtectionLevel protection;
    private FinanceLevel finance;
    private CapitalizationLevel capitalization;
    private OfficeLevel office;


    @OneToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
    private GlobalProfile owner;

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    private List<Business> businesses;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "headquarters_id", referencedColumnName = "id", insertable = false, updatable = false)
    private ChatGroup headquarters;

    public Company(Long id, String name) {
        this.id = id;
        this.name = name;
        this.collectingDate = LocalDate.now().plusDays(1);
        this.normanCoins = this.reputation = 0;
        this.accounting = AccountingLevel.L0;
        this.securities = SecuritiesLevel.L0;
        this.protection = ProtectionLevel.L0;
        this.finance = FinanceLevel.L0;
        this.capitalization = CapitalizationLevel.L0;
        this.office = OfficeLevel.L0;
    }

    public String getName() {
        String name = this.name;
        if(rightEmoji != null) name = name + " " + rightEmoji.getEmoji();
        if(leftEmoji != null) name = leftEmoji.getEmoji() + " " + name;
        return name;
    }
}
