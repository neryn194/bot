package com.neryn.norman.entity;

import com.neryn.norman.enums.Item;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class ExclusiveItem {

    @Id
    private Item item;
    private int count;
}
