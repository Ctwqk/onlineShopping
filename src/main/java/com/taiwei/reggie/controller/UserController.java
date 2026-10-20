package com.taiwei.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taiwei.reggie.common.R;
import com.taiwei.reggie.entity.User;
import com.taiwei.reggie.config.ReggieProperties;
import com.taiwei.reggie.filter.LoginCheckFilter;
import com.taiwei.reggie.notify.VerificationCodeService;
import com.taiwei.reggie.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Customer sign-in with a one-time code (passwordless). The first successful login creates the customer account.
 */
@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name = "Customer login")
public class UserController {

    private final UserService userService;
    private final VerificationCodeService verificationCodeService;
    private final ReggieProperties properties;
    private final Environment environment;

    public UserController(UserService userService, VerificationCodeService verificationCodeService,
                          ReggieProperties properties, Environment environment) {
        this.userService = userService;
        this.verificationCodeService = verificationCodeService;
        this.properties = properties;
        this.environment = environment;
    }

    /** Sends (or, with the default log channel, logs) a one-time code to the phone number / email. */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user) {
        String target = user.getPhone();
        if (!StringUtils.hasText(target)) {
            return R.error("phone number is required");
        }
        String code = verificationCodeService.issue(target.trim());
        R<String> response = R.success("sent");
        if (environment.acceptsProfiles(Profiles.of("dev")) && properties.getVerification().isExposeCode()) {
            response.add("devCode", code); // demo mode only, see reggie.verification.expose-code
        }
        return response;
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Object phoneValue = body.get("phone");
        Object codeValue = body.get("code");
        if (phoneValue == null || codeValue == null) {
            return R.error("phone number and code are required");
        }
        String phone = phoneValue.toString().trim();
        if (!verificationCodeService.verify(phone, codeValue.toString())) {
            return R.error("wrong or expired verification code");
        }
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setStatus(1);
            userService.save(user);
        } else if (Integer.valueOf(0).equals(user.getStatus())) {
            return R.error("this account is disabled");
        }
        request.getSession();
        request.changeSessionId(); // prevent session fixation
        request.getSession().setAttribute(LoginCheckFilter.USER_SESSION_KEY, user.getId());
        return R.success(user);
    }

    @PostMapping("/loginout")
    public R<String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(LoginCheckFilter.USER_SESSION_KEY);
        }
        return R.success("logged out");
    }
}
