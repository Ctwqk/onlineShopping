package com.taiwei.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taiwei.reggie.Utils.SMSUtils;
import com.taiwei.reggie.Utils.ValidateCodeUtils;
import com.taiwei.reggie.common.R;
import com.taiwei.reggie.entity.User;
import com.taiwei.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;


    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        String phone = user.getPhone();
        if(StringUtils.isNotEmpty(phone)){
            String code = ValidateCodeUtils.generateValidateCode(4).toString();

            log.info("code={}",code);
            //SMSUtils.sendMessage("reggie","",phone,code);

            session.setAttribute(phone,code);
            return R.success("sent");
        }
        return R.error("failed to sent");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map user, HttpSession session){
        log.info(user.toString());

        String phone = user.get("phone").toString();
        String code = user.get("code").toString();
        String correctCode = session.getAttribute(phone).toString();
        if(correctCode!=null&&correctCode.equals(code)){
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User curUser = userService.getOne(queryWrapper);
            if(curUser==null){
                curUser = new User();
                curUser.setPhone(phone);
                userService.save(curUser);
            }
            session.setAttribute("user",curUser.getId());
            return R.success(curUser);
        }

        return R.error("failed");
    }
}
