package com.taiwei.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taiwei.reggie.Utils.SMSUtils;
import com.taiwei.reggie.Utils.ValidateCodeUtils;
import com.taiwei.reggie.Utils.ValidateEmailUtils;
import com.taiwei.reggie.common.R;
import com.taiwei.reggie.entity.User;
import com.taiwei.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        String phone = user.getPhone();
        if(StringUtils.isNotEmpty(phone)){
            String code = ValidateCodeUtils.generateValidateCode(4).toString();

            log.info("code={}",code);
            //SMSUtils.sendMessage("reggie","",phone,code);
            //ValidateEmailUtils.sendSimpleEmail(phone, "Validation Email---Do Not Reply", code);

            //session.setAttribute(phone,code);
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);


            return R.success("sent");
        }
        return R.error("failed to sent");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map user, HttpSession session){
        log.info(user.toString());

        String phone = user.get("phone").toString();
        String code = user.get("code").toString();
        Object correctCode = redisTemplate.opsForValue().get(phone);
        //Object correctCode = session.getAttribute(phone);
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
            redisTemplate.delete(phone);
            return R.success(curUser);
        }

        return R.error("failed");
    }
}
