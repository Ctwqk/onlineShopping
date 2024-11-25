package com.taiwei.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taiwei.reggie.controller.ShoppingCartController;
import com.taiwei.reggie.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {
}
