package com.taiwei.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taiwei.reggie.common.BaseContext;
import com.taiwei.reggie.common.R;
import com.taiwei.reggie.entity.Employee;
import com.taiwei.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * log employee in
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        /**
         * check whether user exist
         * get the md5 password
         * check md5 password in database
         * check whether user is banned
         * set employee id to session
         */
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee resultEmp  = employeeService.getOne(queryWrapper);
        if(resultEmp == null){
            return R.error("user with username: "+employee.getUsername()+" not found");
        }
        if(!resultEmp.getPassword().equals(password)){
            return R.error("password or username wrong");
        }
        if(resultEmp.getStatus()==0){
            return R.error("user banned");
        }
        request.getSession().setAttribute("employee",resultEmp.getId());
        return R.success(resultEmp);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //clear the ids of employees in session
        request.removeAttribute("employee");
        return R.success("logged out");

    }

    /**
     * add new employee
     * @param employee
     * @return
     */

    @PostMapping

    public R<String> save(@RequestBody Employee employee){
        log.info("new employee adding: {}",employee.toString());

        //initial pwd is md5 encrypted 123456
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());

        //long createrID = (long) request.getSession().getAttribute("employee");

        //employee.setCreateUser(createrID);
        //employee.setUpdateUser(createrID);

        employeeService.save(employee);
        return R.success("updated");
    }
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize,String name){
        log.info("page = {}, page size = {},name ={}",page, pageSize,name);

        //paginize
        Page pageInfo = new Page(page,pageSize);

        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();

        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);

        queryWrapper.orderByDesc(Employee::getUpdateTime);

        employeeService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);

    }

    @PutMapping
    public R<String> update(@RequestBody Employee employee){

        employeeService.updateById(employee);

        return R.success("updated");
    }

    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable long id){
        Employee result = employeeService.getById(id);
        return result!=null?R.success(result):R.error("no employee found");
    }


}
