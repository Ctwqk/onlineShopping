package com.taiwei.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taiwei.reggie.entity.AddressBook;
import com.taiwei.reggie.mapper.AddressBookMapper;
import com.taiwei.reggie.service.AddressBookService;
import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

}
