package com.taiwei.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taiwei.reggie.entity.Employee;

public interface EmployeeService extends IService<Employee> {

    /** Initial password given to employees created from the admin console. */
    String INITIAL_PASSWORD = "123456";

    /**
     * Verifies the credentials. A legacy MD5 hash is upgraded to BCrypt on success.
     *
     * @throws com.taiwei.reggie.common.CustomException for unknown user, wrong password or disabled account
     */
    Employee authenticate(String username, String password);

    /** Creates an employee with {@link #INITIAL_PASSWORD} (BCrypt-hashed) and status enabled. */
    Employee create(Employee employee);

    /** Updates profile fields / status; never touches the password. */
    void updateProfile(Employee employee);
}
