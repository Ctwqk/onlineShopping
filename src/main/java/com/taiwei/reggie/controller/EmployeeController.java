package com.taiwei.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taiwei.reggie.common.R;
import com.taiwei.reggie.entity.Employee;
import com.taiwei.reggie.filter.LoginCheckFilter;
import com.taiwei.reggie.service.EmployeeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

@Slf4j
@RestController
@RequestMapping("/employee")
@Tag(name = "Employees (admin)")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /** Admin console login. The employee id is stored in the HTTP session. */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        Employee authenticated = employeeService.authenticate(employee.getUsername(), employee.getPassword());
        request.getSession(); // make sure a session exists...
        request.changeSessionId(); // ...and rotate its id to prevent session fixation
        request.getSession().setAttribute(LoginCheckFilter.EMPLOYEE_SESSION_KEY, authenticated.getId());
        return R.success(authenticated);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(LoginCheckFilter.EMPLOYEE_SESSION_KEY);
        }
        return R.success("logged out");
    }

    @PostMapping
    public R<String> save(@RequestBody Employee employee) {
        log.info("adding employee {}", employee.getUsername());
        employeeService.create(employee);
        return R.success("employee added");
    }

    @GetMapping("/page")
    public R<Page<Employee>> page(int page, int pageSize, String name) {
        Page<Employee> pageInfo = new Page<>(page, pageSize);
        employeeService.page(pageInfo, new LambdaQueryWrapper<Employee>()
                .like(StringUtils.hasText(name), Employee::getName, name)
                .orderByDesc(Employee::getUpdateTime));
        return R.success(pageInfo);
    }

    @PutMapping
    public R<String> update(@RequestBody Employee employee) {
        employeeService.updateProfile(employee);
        return R.success("updated");
    }

    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable long id) {
        Employee result = employeeService.getById(id);
        return result != null ? R.success(result) : R.error("no employee found");
    }
}
