package com.neryn.norman.enums;

public record ItemCount(Item item, int count) {
    public Item getEmoji() {
        return this.item;
    }
    public int getCount() {
        return this.count;
    }
}
