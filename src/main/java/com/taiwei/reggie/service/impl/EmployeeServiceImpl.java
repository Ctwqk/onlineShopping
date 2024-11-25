package com.taiwei.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taiwei.reggie.entity.Employee;
import com.taiwei.reggie.mapper.EmployeeMapper;
import com.taiwei.reggie.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

}
