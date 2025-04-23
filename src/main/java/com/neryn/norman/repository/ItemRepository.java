package com.neryn.norman.repository;

import com.neryn.norman.entity.ItemToUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<ItemToUser, ItemToUser.ItemToUserPK> {

    @Query(
            nativeQuery = true,
            value = "SELECT * FROM item_to_user " +
                    "WHERE user_id = :userId " +
                    "ORDER BY item DESC " +
                    "LIMIT :limit OFFSET :offset"
    )
    List<ItemToUser> findPageUserEmojies(Long userId, int limit, int offset);
}
