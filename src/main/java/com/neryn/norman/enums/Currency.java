package com.neryn.norman.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum Currency {
    STARS   ("✨",           "звёзды",    "звёзд"),
    NCOINS(  "\uD83C\uDF65", "NC",        "NC"),
    DIAMONDS("\uD83D\uDC8E", "кристаллы", "кристаллов"),
    COINS   ("\uD83C\uDF15", "монеты",    "монет");

    private final String emoji, nominative, genetive;

    public String low() {
        return EmojiEnum.ERROR.getValue() + " У вас нет нужного количества " + this.genetive;
    }

    public String low(int coins) {
        return String.format("%s У вас нет нужного количества %s. Нужно %d",
                EmojiEnum.ERROR.getValue(), this.genetive, coins);
    }
}
