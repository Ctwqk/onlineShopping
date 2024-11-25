package com.taiwei.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taiwei.reggie.entity.ShoppingCart;
import com.taiwei.reggie.mapper.ShoppingCartMapper;
import com.taiwei.reggie.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
