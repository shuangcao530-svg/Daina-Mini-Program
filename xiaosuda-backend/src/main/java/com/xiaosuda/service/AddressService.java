package com.xiaosuda.service;

import com.xiaosuda.entity.Address;

import java.util.List;

public interface AddressService {
    List<Address> list(Long userId);
    Address create(Long userId, Address address);
    Address update(Long userId, Long addressId, Address address);
    void delete(Long userId, Long addressId);
    void setDefault(Long userId, Long addressId);
}
