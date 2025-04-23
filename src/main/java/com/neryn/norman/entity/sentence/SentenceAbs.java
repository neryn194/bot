package com.neryn.norman.entity.sentence;

import com.neryn.norman.entity.GroupProfile;

import java.time.LocalDateTime;

public abstract class SentenceAbs {
    public abstract Long getChatId();
    public abstract Long getUserId();
    public abstract Long getModerId();
    public abstract GroupProfile getProfile();
    public abstract GroupProfile getModer();
    public abstract LocalDateTime getTime();
    public abstract String getDescription();
}
