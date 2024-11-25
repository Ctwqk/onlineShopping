package com.taiwei.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taiwei.reggie.entity.User;
import com.taiwei.reggie.mapper.UserMapper;
import com.taiwei.reggie.service.UserService;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
