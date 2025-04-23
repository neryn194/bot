package com.neryn.norman.entity.sentence;

import java.time.LocalDateTime;

import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.entity.GroupProfile;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Warn extends SentenceAbs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "moder_id")
    private Long moderId;
    private LocalDateTime time;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", insertable = false, updatable = false)
    private ChatGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false),
            @JoinColumn(name = "chat_id", referencedColumnName = "chat_id", insertable = false, updatable = false)
    })
    private GroupProfile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "moder_id", referencedColumnName = "user_id", insertable = false, updatable = false),
            @JoinColumn(name = "chat_id", referencedColumnName = "chat_id", insertable = false, updatable = false)
    })
    private GroupProfile moder;

    public Warn(Long userId, Long chatId, Long moderId, LocalDateTime time, String description) {
        this.userId = userId;
        this.chatId = chatId;
        this.moderId = moderId;
        this.time = time;
        this.description = description;
    }
}
