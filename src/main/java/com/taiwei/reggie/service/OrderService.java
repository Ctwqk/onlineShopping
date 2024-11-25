package com.taiwei.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taiwei.reggie.entity.Orders;

public interface OrderService extends IService<Orders> {
    public void submit(Orders order);
}
