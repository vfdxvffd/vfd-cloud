package com.vfd.demo.controller;

import com.vfd.demo.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @PackageName: com.vfd.cloud.controller
 * @ClassName: HelloController
 * @Description:
 * @author: vfdxvffd
 * @date: 11/26/20 2:39 PM
 */
@Controller
public class HelloController {

    @Autowired
    RedisService redisService;

    //记录日志
    Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 将/映射到登陆页
     * @return
     */
    @RequestMapping("/")
    public String index() {
        return "login";
    }

    /**
     * 映射到注册页面
     * @return
     */
    @RequestMapping("/pages/register")
    public String register() {
        return "register";
    }

    /**
     * 映射到忘记密码页面
     * @return
     */
    @RequestMapping("/pages/forgot-password")
    public String forget_password() {
        return "forgot-password";
    }

    /**
     * 映射到重置密码页面
     * @param uuid
     * @param email
     * @return
     */
    @RequestMapping("/pages/reset-password")
    public ModelAndView reset_password(String uuid, String email) {
        String id = (String) redisService.get(email + ":uuid");
        ModelAndView modelAndView = null;
        if (uuid.equals(id)) {
            modelAndView = new ModelAndView("reset-password");
            modelAndView.addObject("email",email);
        } else {
            modelAndView = new ModelAndView("blank");
            logger.warn("reset_password:用户重置密码链接已失效，email：" + email);
        }
        return modelAndView;
    }
}