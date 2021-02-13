package com.vfd.demo.interceptor;

import com.vfd.demo.bean.UserAccInfo;
import com.vfd.demo.service.UserLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @PackageName: com.vfd.demo.interceptor
 * @ClassName: LoginInterceptor
 * @Description:
 * @author: vfdxvffd
 * @date: 2021/1/27 下午12:53
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    UserLoginService userLoginService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("拦截了：" + request.getRequestURL());
        HttpSession session = request.getSession();
        Object loginUserId = session.getAttribute("loginUserId");
        Object loginUserName = session.getAttribute("loginUserName");
        //&& loginUserName.equals(userLoginService.getNameById((Integer) loginUserId))
        if (loginUserId != null && loginUserName != null) { //如果session中有信息说明已经登录
            return true;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            //request.setAttribute("err","无权限访问，请先登陆");
            request.getRequestDispatcher("/pages/login").forward(request,response);
            return false;
        }
        String userEmail = null;
        String userPassword = null;
        for (Cookie item:cookies) {
            if ("loginEmail".equals(item.getName())) {
                userEmail = item.getValue();
            } else if ("loginPassword".equals(item.getName())) {
                userPassword = item.getValue();
            }
        }
        if (userEmail == null || userPassword == null) {
            //request.setAttribute("err","无权限访问，请先登陆");
            request.getRequestDispatcher("/pages/login").forward(request,response);
            return false;
        }
        UserAccInfo login = userLoginService.login(userEmail);
        if (login == null || !userPassword.equals(login.getPassword())) {
            //request.setAttribute("err","无权限访问，请先登陆");
            request.getRequestDispatcher("/pages/login").forward(request,response);
            return false;
        }
        session.setAttribute("loginUserId",login.getId());
        session.setAttribute("loginUserName",login.getName());
        return true;
    }
}