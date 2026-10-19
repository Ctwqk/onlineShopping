package com.taiwei.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taiwei.reggie.common.CustomException;
import com.taiwei.reggie.entity.Employee;
import com.taiwei.reggie.mapper.EmployeeMapper;
import com.taiwei.reggie.security.PasswordService;
import com.taiwei.reggie.service.EmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    private final PasswordService passwordService;

    public EmployeeServiceImpl(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @Override
    public Employee authenticate(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new CustomException("username and password are required");
        }
        Employee employee = this.getOne(new LambdaQueryWrapper<Employee>().eq(Employee::getUsername, username));
        // same message for "no such user" and "wrong password": do not reveal which usernames exist
        if (employee == null || !passwordService.matches(password, employee.getPassword())) {
            throw new CustomException("password or username wrong");
        }
        if (Integer.valueOf(0).equals(employee.getStatus())) {
            throw new CustomException("user banned");
        }
        if (passwordService.isLegacy(employee.getPassword())) {
            Employee upgrade = new Employee();
            upgrade.setId(employee.getId());
            upgrade.setPassword(passwordService.hash(password));
            this.updateById(upgrade);
        }
        return employee;
    }

    @Override
    public Employee create(Employee employee) {
        employee.setId(null);
        employee.setPassword(passwordService.hash(INITIAL_PASSWORD));
        if (employee.getStatus() == null) {
            employee.setStatus(1);
        }
        this.save(employee);
        return employee;
    }

    @Override
    public void updateProfile(Employee employee) {
        employee.setPassword(null);
        this.updateById(employee);
    }
}
