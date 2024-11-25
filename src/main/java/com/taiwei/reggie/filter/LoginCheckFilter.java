package com.taiwei.reggie.filter;


import com.taiwei.reggie.common.BaseContext;
import com.taiwei.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

//import static org.apache.logging.log4j.message.MapMessage.MapFormat.JSON;
import com.alibaba.fastjson.JSON;
/**
 * check whether user logged in
 */

@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    public boolean judgeSession(String userType,HttpSession session){
        return session.getAttribute(userType)!=null;
    }
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        /**
         * check the uri of request
         * whether need to check
         * check if is logged in
         */
        String requestURI = request.getRequestURI();
        String[] freeURLs = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/sendMsg",
                "/user/login"
        };

        boolean check = check(requestURI,freeURLs);
        if(check){
            filterChain.doFilter(request,response);
            return;
        }

        if(judgeSession("employee",request.getSession())){
            Long id = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentID(id);
            filterChain.doFilter(request,response);
            return;
        }
        else if(judgeSession("user",request.getSession())){
            Long id = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentID(id);
            filterChain.doFilter(request,response);
            return;
        }
        else{
            response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        }



        return;
    }

    public boolean check(String requestURI,String[] freeURLs){
        for(String url: freeURLs){
            if(PATH_MATCHER.match(url,requestURI)){
                return true;
            }
        }
        return false;
    }
}
