package com.vfd.demo.controller;

import com.vfd.demo.bean.FileInfo;
import com.vfd.demo.service.FileOperationService;
import com.vfd.demo.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

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
    @Autowired
    FileOperationService fileOperationService;
    @Autowired
    RabbitTemplate rabbitTemplate;


    /**
     * 映射到登陆页
     * @return
     */
    @RequestMapping("/pages/login")
    public String index() {
        rabbitTemplate.convertAndSend("visit.direct","visit", "null");
        return "login";
    }

    @RequestMapping("/")
    public ModelAndView login(HttpSession session) {
        rabbitTemplate.convertAndSend("visit.direct","visit", "null");
        Integer userId = (Integer) session.getAttribute("loginUserId");
        String userName = (String) session.getAttribute("loginUserName");
        ModelAndView modelAndView = new ModelAndView("index");
        modelAndView.addObject("username",userName);
        modelAndView.addObject("id",userId);     //将用户id发送到index页面
        modelAndView.addObject("currentDir",fileOperationService.getFileById(-1*userId, userId,0)); //用户根文件夹id        modelAndView.addObject("location","");  //位置
        List<Integer> pidByLocal = fileOperationService.getPidByLocal(">" + (-1 * userId) + ".全部文件");
        List<FileInfo> all = new ArrayList<>();
        if (pidByLocal.size() > 0) {
            all = fileOperationService.getFilesByFid(pidByLocal.get(0), userId);    //所有文件
        }
        AccountController.getDirsAndDocs(modelAndView, all);
        ArrayList<FileInfo> fileInfos = new ArrayList<>();
        modelAndView.addObject("path",fileInfos);
        modelAndView.addObject("location","");  //位置
        return modelAndView;
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
        }
        return modelAndView;
    }
}