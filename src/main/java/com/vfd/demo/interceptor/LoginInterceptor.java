package com.vfd.demo.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @PackageName: com.vfd.demo.interceptor
 * @ClassName: LoginInterceptor
 * @Description:
 * @author: vfdxvffd
 * @date: 2021/1/27 下午12:53
 */
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Object userId = request.getSession().getAttribute("loginUserId");
        if (userId == null) {
            request.setAttribute("err","无权限访问，请先登陆");
            request.getRequestDispatcher("/").forward(request,response);
            //response.sendRedirect("/");
            return false;
        } else {
            return true;
        }
    }
}