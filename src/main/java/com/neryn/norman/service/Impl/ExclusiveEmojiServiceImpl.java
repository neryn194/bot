package com.neryn.norman.service.Impl;

import com.neryn.norman.entity.ExclusiveItem;
import com.neryn.norman.enums.Item;
import com.neryn.norman.repository.ExclusiveItemRepository;
import com.neryn.norman.service.ExclusiveEmojiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExclusiveEmojiServiceImpl implements ExclusiveEmojiService {

    private final ExclusiveItemRepository repository;

    public ExclusiveItem findByEmoji(Item item) {
        return repository.findById(item).orElse(null);
    }
    public List<ExclusiveItem> findAll() {
        return repository.findAllInSale();
    }

    public void save(ExclusiveItem item) {
        repository.save(item);
    }
    public void delete(ExclusiveItem item) {
        repository.delete(item);
    }
}
