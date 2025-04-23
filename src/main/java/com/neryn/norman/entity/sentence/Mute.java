package com.neryn.norman.entity.sentence;

import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.entity.GroupProfile;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Mute extends SentenceAbs {

    @EmbeddedId
    private GroupProfile.GroupProfilePK id;

    @Column(name = "moder_id")
    private Long moderId;
    private LocalDateTime time;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", insertable = false, updatable = false)
    private ChatGroup group;

    @OneToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumns({
            @PrimaryKeyJoinColumn(name = "user_id", referencedColumnName = "user_id"),
            @PrimaryKeyJoinColumn(name = "chat_id", referencedColumnName = "chat_id")
    })
    private GroupProfile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "moder_id", referencedColumnName = "user_id", insertable = false, updatable = false),
            @JoinColumn(name = "chat_id", referencedColumnName = "chat_id", insertable = false, updatable = false)
    })
    private GroupProfile moder;

    public Mute(Long userId, Long chatId, Long moderId, LocalDateTime time, String description) {
        this.id = new GroupProfile.GroupProfilePK(userId, chatId);
        this.moderId = moderId;
        this.time = time;
        this.description = description;
    }

    public Long getChatId() {
        return id.getChatId();
    }

    public Long getUserId() {
        return id.getUserId();
    }
}
