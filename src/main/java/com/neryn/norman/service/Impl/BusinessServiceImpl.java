package com.neryn.norman.service.Impl;

import com.neryn.norman.entity.Business;
import com.neryn.norman.repository.BusinessRepository;
import com.neryn.norman.service.BusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository repository;

    public Business findById(Integer id) {
        return repository.findById(id).orElse(null);
    }
    public List<Business> findAllFromSale() {
        return repository.findAllFromSale();
    }
    public List<Business> findAllByOwnerId(Long ownerId) {
        return repository.findAllByOwnerId(ownerId);
    }

    public void save(Business business) {
        repository.save(business);
    }
    public void saveAll(List<Business> businesses) {
        repository.saveAll(businesses);
    }
}
