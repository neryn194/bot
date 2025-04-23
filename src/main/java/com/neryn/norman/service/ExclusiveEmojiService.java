package com.neryn.norman.service;

import com.neryn.norman.entity.ExclusiveItem;
import com.neryn.norman.enums.Item;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ExclusiveEmojiService {

    ExclusiveItem findByEmoji(Item item);
    List<ExclusiveItem> findAll();

    void save(ExclusiveItem item);
    void delete(ExclusiveItem item);
}
