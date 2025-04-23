package com.neryn.norman.service.Impl;

import com.neryn.norman.entity.ItemToUser;
import com.neryn.norman.enums.Item;
import com.neryn.norman.repository.ItemRepository;
import com.neryn.norman.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository repository;

    public ItemToUser findById(Long userId, Item item) {
        return repository.findById(new ItemToUser.ItemToUserPK(userId, item)).orElse(null);
    }
    public List<ItemToUser> findPageUserEmojies(Long userId, int limit, int page) {
        return repository.findPageUserEmojies(userId, limit + 1, (page - 1) * limit);
    }

    public void save(ItemToUser item) {
        repository.save(item);
    }
    public void delete(ItemToUser item) {
        repository.delete(item);
    }
    public void saveAll(List<ItemToUser> items) {
        repository.saveAll(items);
    }
    public void deleteAll(List<ItemToUser> items) {
        repository.deleteAllInBatch(items);
    }
}
