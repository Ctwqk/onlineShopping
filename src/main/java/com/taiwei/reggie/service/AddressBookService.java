package com.taiwei.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taiwei.reggie.entity.AddressBook;

import java.util.List;

public interface AddressBookService extends IService<AddressBook> {

    /** Saves a new address for the customer; the very first address becomes the default one. */
    AddressBook addFor(Long userId, AddressBook addressBook);

    /** Updates an address the customer owns. */
    AddressBook updateFor(Long userId, AddressBook addressBook);

    /** Marks one address as the default and clears the flag on all others (atomically). */
    AddressBook setDefault(Long userId, Long addressId);

    /** Address by id, only if it belongs to the customer; otherwise null. */
    AddressBook findOwned(Long userId, Long addressId);

    AddressBook findDefault(Long userId);

    List<AddressBook> listFor(Long userId);

    void deleteOwned(Long userId, List<Long> ids);
}
