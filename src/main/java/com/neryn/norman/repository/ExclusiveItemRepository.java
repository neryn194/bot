package com.neryn.norman.repository;

import com.neryn.norman.entity.ExclusiveItem;
import com.neryn.norman.enums.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExclusiveItemRepository extends JpaRepository<ExclusiveItem, Item> {

    @Query(value = "SELECT item FROM ExclusiveItem item " +
            "WHERE count > 0 " +
            "ORDER BY item.item ASC")
    List<ExclusiveItem> findAllInSale();
}
