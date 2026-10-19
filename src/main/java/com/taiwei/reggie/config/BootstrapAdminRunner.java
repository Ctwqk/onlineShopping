package com.taiwei.reggie.config;

import com.taiwei.reggie.entity.Employee;
import com.taiwei.reggie.security.PasswordService;
import com.taiwei.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Without seed data a fresh database has no administrator and nobody could ever log in to the console. If
 * {@code REGGIE_ADMIN_PASSWORD} is set and the employee table is empty, this creates the first administrator.
 */
@Slf4j
@Component
public class BootstrapAdminRunner implements ApplicationRunner {

    private final EmployeeService employeeService;
    private final PasswordService passwordService;
    private final ReggieProperties.BootstrapAdmin settings;

    public BootstrapAdminRunner(EmployeeService employeeService, PasswordService passwordService,
                                ReggieProperties properties) {
        this.employeeService = employeeService;
        this.passwordService = passwordService;
        this.settings = properties.getBootstrapAdmin();
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(settings.getPassword()) || employeeService.count() > 0) {
            return;
        }
        Employee admin = new Employee();
        admin.setUsername(settings.getUsername());
        admin.setName(settings.getName());
        admin.setPassword(passwordService.hash(settings.getPassword()));
        admin.setPhone("00000000000");
        admin.setSex("1");
        admin.setIdNumber("000000000000000000");
        admin.setStatus(1);
        employeeService.save(admin);
        log.info("created initial administrator '{}'", settings.getUsername());
    }
}
