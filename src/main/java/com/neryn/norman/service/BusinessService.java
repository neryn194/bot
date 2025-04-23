package com.neryn.norman.service;

import com.neryn.norman.entity.Business;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BusinessService {

    Business findById(Integer id);
    List<Business> findAllFromSale();
    List<Business> findAllByOwnerId(Long ownerId);
    void save(Business business);
    void saveAll(List<Business> businesses);
}
