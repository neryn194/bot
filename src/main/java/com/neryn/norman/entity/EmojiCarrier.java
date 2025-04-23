package com.neryn.norman.entity;

import com.neryn.norman.enums.Item;

public interface EmojiCarrier {
    void setLeftEmoji(Item item);
    void setRightEmoji(Item item);
    Item getLeftEmoji();
    Item getRightEmoji();
}
