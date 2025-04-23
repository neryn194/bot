package com.neryn.norman.service;

import com.neryn.norman.entity.ItemToUser;
import com.neryn.norman.enums.Item;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ItemService {
    ItemToUser findById(Long userId, Item item);
    List<ItemToUser> findPageUserEmojies(Long userId, int limit, int page);

    void save(ItemToUser item);
    void delete(ItemToUser item);
    void saveAll(List<ItemToUser> items);
    void deleteAll(List<ItemToUser> items);
}
