package com.taiwei.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taiwei.reggie.common.CustomException;
import com.taiwei.reggie.entity.AddressBook;
import com.taiwei.reggie.mapper.AddressBookMapper;
import com.taiwei.reggie.service.AddressBookService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

    @Override
    @Transactional
    public AddressBook addFor(Long userId, AddressBook addressBook) {
        addressBook.setId(null);
        addressBook.setUserId(userId);
        boolean first = this.count(new LambdaQueryWrapper<AddressBook>().eq(AddressBook::getUserId, userId)) == 0;
        addressBook.setIsDefault(first ? 1 : 0);
        this.save(addressBook);
        return addressBook;
    }

    @Override
    @Transactional
    public AddressBook updateFor(Long userId, AddressBook addressBook) {
        if (findOwned(userId, addressBook.getId()) == null) {
            throw new CustomException("address not found");
        }
        addressBook.setUserId(userId);
        addressBook.setIsDefault(null); // the default flag is only changed through setDefault()
        this.updateById(addressBook);
        return this.getById(addressBook.getId());
    }

    @Override
    @Transactional
    public AddressBook setDefault(Long userId, Long addressId) {
        if (findOwned(userId, addressId) == null) {
            throw new CustomException("address not found");
        }
        this.update(new LambdaUpdateWrapper<AddressBook>()
                .eq(AddressBook::getUserId, userId).set(AddressBook::getIsDefault, 0));
        this.update(new LambdaUpdateWrapper<AddressBook>()
                .eq(AddressBook::getId, addressId).set(AddressBook::getIsDefault, 1));
        return this.getById(addressId);
    }

    @Override
    public AddressBook findOwned(Long userId, Long addressId) {
        if (addressId == null) {
            return null;
        }
        return this.getOne(new LambdaQueryWrapper<AddressBook>()
                .eq(AddressBook::getId, addressId).eq(AddressBook::getUserId, userId));
    }

    @Override
    public AddressBook findDefault(Long userId) {
        return this.getOne(new LambdaQueryWrapper<AddressBook>()
                .eq(AddressBook::getUserId, userId).eq(AddressBook::getIsDefault, 1).last("LIMIT 1"));
    }

    @Override
    public List<AddressBook> listFor(Long userId) {
        return this.list(new LambdaQueryWrapper<AddressBook>()
                .eq(AddressBook::getUserId, userId)
                .orderByDesc(AddressBook::getIsDefault)
                .orderByDesc(AddressBook::getUpdateTime));
    }

    @Override
    @Transactional
    public void deleteOwned(Long userId, List<Long> ids) {
        this.remove(new LambdaQueryWrapper<AddressBook>()
                .eq(AddressBook::getUserId, userId).in(AddressBook::getId, ids));
        // if the default address was deleted, promote the most recently updated remaining one
        if (findDefault(userId) == null) {
            List<AddressBook> remaining = listFor(userId);
            if (!remaining.isEmpty()) {
                setDefault(userId, remaining.get(0).getId());
            }
        }
    }
}
