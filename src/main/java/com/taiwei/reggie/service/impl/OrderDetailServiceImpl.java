package com.taiwei.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taiwei.reggie.entity.OrderDetail;
import com.taiwei.reggie.mapper.OrderDetailMapper;
import com.taiwei.reggie.service.OrderDetailService;
import org.springframework.stereotype.Service;


@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {

}
