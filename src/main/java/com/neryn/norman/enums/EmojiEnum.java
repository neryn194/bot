package com.neryn.norman.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EmojiEnum {
    SUCCESFUL("✅"),
    ERROR(    "⛔"),
    WARNING(  "⚠"),
    HELP(     "\uD83D\uDCD8"),
    CHAT(     "\uD83D\uDCAC"),
    BUST(     "\uD83D\uDC64");

    private final String value;
}
