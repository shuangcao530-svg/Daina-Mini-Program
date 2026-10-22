package com.xiaosuda.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaosuda.common.BusinessException;
import com.xiaosuda.common.ErrorCode;
import com.xiaosuda.entity.Address;
import com.xiaosuda.mapper.AddressMapper;
import com.xiaosuda.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public List<Address> list(Long userId) {
        return addressMapper.selectList(
                new LambdaQueryWrapper<Address>()
                        .eq(Address::getUserId, userId)
                        .orderByDesc(Address::getIsDefault)
                        .orderByDesc(Address::getCreatedAt));
    }

    @Override
    @Transactional
    public Address create(Long userId, Address address) {
        address.setUserId(userId);
        address.setIsDefault(address.getIsDefault() != null ? address.getIsDefault() : 0);
        addressMapper.insert(address);

        if (address.getIsDefault() == 1) {
            clearOtherDefaults(userId, address.getId());
        }
        return address;
    }

    @Override
    @Transactional
    public Address update(Long userId, Long addressId, Address address) {
        Address existing = getAndCheckOwner(userId, addressId);
        if (address.getTag() != null) existing.setTag(address.getTag());
        if (address.getDetail() != null) existing.setDetail(address.getDetail());
        if (address.getContact() != null) existing.setContact(address.getContact());
        if (address.getPhone() != null) existing.setPhone(address.getPhone());
        if (address.getIsDefault() != null) {
            existing.setIsDefault(address.getIsDefault());
            if (address.getIsDefault() == 1) {
                clearOtherDefaults(userId, addressId);
            }
        }
        addressMapper.updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public void delete(Long userId, Long addressId) {
        getAndCheckOwner(userId, addressId);
        addressMapper.deleteById(addressId);
    }

    @Override
    @Transactional
    public void setDefault(Long userId, Long addressId) {
        getAndCheckOwner(userId, addressId);
        clearOtherDefaults(userId, addressId);
        Address address = addressMapper.selectById(addressId);
        address.setIsDefault(1);
        addressMapper.updateById(address);
    }

    private Address getAndCheckOwner(Long userId, Long addressId) {
        Address addr = addressMapper.selectById(addressId);
        if (addr == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "地址不存在");
        }
        if (!addr.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该地址");
        }
        return addr;
    }

    private void clearOtherDefaults(Long userId, Long excludeId) {
        List<Address> defaults = addressMapper.selectList(
                new LambdaQueryWrapper<Address>()
                        .eq(Address::getUserId, userId)
                        .eq(Address::getIsDefault, 1)
                        .ne(Address::getId, excludeId));
        for (Address a : defaults) {
            a.setIsDefault(0);
            addressMapper.updateById(a);
        }
    }
}
