package com.vfd.demo.controller;

import com.vfd.demo.bean.FileInfo;
import com.vfd.demo.bean.UserAccInfo;
import com.vfd.demo.exception.VerificationCodeLengthException;
import com.vfd.demo.service.FileOperationService;
import com.vfd.demo.service.RedisService;
import com.vfd.demo.service.UserLoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @PackageName: com.vfd.cloud.controller
 * @ClassName: AccountController
 * @Description:
 * @author: vfdxvffd
 * @date: 11/26/20 4:17 PM
 */
@Controller
public class AccountController {

    @Autowired
    FileOperationService fileOperationService;
    @Autowired
    RedisService redisService;
    @Autowired
    UserLoginService userLoginService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    //记录日志
    Logger logger = LoggerFactory.getLogger(getClass());
    //发送验证码的地址
    String SOURCE_EMAIL_ADDRESS = "vfdxvffd@qq.com";


    /**
     * 发送验证码到用户邮箱
     * @param email
     * @return
     */
    @ResponseBody
    @RequestMapping("/sendCode")
    public String sendCode(String email) {
        String verificationCode = null;
        verificationCode = getVerificationCode();
        //先检查是否已存在此邮箱
        if (userLoginService.isExist(email)) {
            return "exitEmail";
        }
        Map<String,String> map = new HashMap<>(9);
        //发送验证码
        map.put("source_email",SOURCE_EMAIL_ADDRESS);
        map.put("dest_email",email);
        map.put("title","vfd-cloud");
        map.put("context","your verification code is : " + verificationCode);
        map.put("html","false");
        //将验证码存到缓存中
        map.put("key",email+":verificationCode");
        map.put("val",verificationCode);
        map.put("time","60");
        //记录日志
        map.put("log","sendCode:用户注册发送验证码,email:" + email);
        rabbitTemplate.convertAndSend("account.topic","account.sendCode",map);
        return "success";
    }

    /**
     * 用户注册
     * @param exampleFirstName
     * @param exampleLastName
     * @param exampleInputEmail
     * @param exampleInputVerification
     * @param exampleInputPassword
     * @param exampleRepeatPassword
     * @return
     */
    @RequestMapping("/register")
    public ModelAndView register(@RequestParam("exampleFirstName") String exampleFirstName,
                                 @RequestParam("exampleLastName") String exampleLastName,
                                 @RequestParam("exampleInputEmail") String exampleInputEmail,
                                 @RequestParam("exampleInputVerification") String exampleInputVerification,
                                 @RequestParam("exampleInputPassword") String exampleInputPassword,
                                 @RequestParam("exampleRepeatPassword") String exampleRepeatPassword  ) {
        ModelAndView modelAndView = new ModelAndView("register");
        //从缓存中获取验证码
        if (!exampleInputPassword.equals(exampleRepeatPassword)) {
            modelAndView.addObject("err","两次输入密码不一致");
            rabbitTemplate.convertAndSend("log.direct","info","register:用户注册时密码不一致,email:" + exampleInputEmail + ";两次的密码分别为:" + exampleInputPassword + "和" + exampleRepeatPassword);
            return modelAndView;
        }
        String verificationCode = (String) redisService.get(exampleInputEmail+":verificationCode");
        if (verificationCode == null) {
            modelAndView.addObject("err","验证码过期");
            rabbitTemplate.convertAndSend("log.direct","info","register:用户注册时验证码过期,email:" + exampleInputEmail);
        } else if (!exampleInputVerification.equals(verificationCode)) {  //验证码错误
            modelAndView.addObject("err","验证码错误");
            rabbitTemplate.convertAndSend("log.direct","info","register:用户注册时验证码错误,email:" + exampleInputEmail + "输入的验证码和正确的分别是：" + exampleInputVerification + "和" + verificationCode);
        } else {    //验证码正确
            Integer register = userLoginService.register(exampleFirstName + exampleLastName, exampleInputEmail, exampleInputPassword);
            if (register == -1) {
                modelAndView.addObject("err","用户邮箱已存在");
                rabbitTemplate.convertAndSend("log.direct","info","register:用户邮箱重复注册，email:" + exampleInputEmail);
            } else if (register == 0) {
                modelAndView.addObject("err","注册失败，请重试");
                rabbitTemplate.convertAndSend("log.direct","error","register：注册失败，可能服务器或数据库出错，email:" + exampleInputEmail);
            } else {
                modelAndView = new ModelAndView("login");
                modelAndView.addObject("msg","注册成功，请登陆");
                //验证设置过期，删除验证码
                Map<String,String> map = new HashMap<>(2);
                map.put("del",exampleInputEmail+":verificationCode");
                map.put("log","register：注册成功！新用户id为：" + register);
                rabbitTemplate.convertAndSend("account.topic","account.register",map);
            }
        }
        return modelAndView;
    }
    

    @ResponseBody
    @RequestMapping("/shareLogin")
    public String shareLogin(@RequestParam("inputEmail") String inputEmail,
                              @RequestParam("inputPassword") String inputPassword,
                              @RequestParam("customCheck") String remember,
                              HttpSession session,
                              HttpServletRequest request,
                              HttpServletResponse response) {
        UserAccInfo login = userLoginService.login(inputEmail);
        if (login == null) {
            return "用户不存在";
        } else if (!login.getPassword().equals(inputPassword)) {
            return "密码错误";
        } else {
            session.setAttribute("loginUserId",login.getId());
            session.setAttribute("loginUserName",login.getName());
            if (!"".equals(remember)) {     //记住我功能
                rememberMe(inputEmail, inputPassword, request, response);
            }
            return "success";
        }
    }

    /**
     * 用户登陆
     * @param exampleInputEmail
     * @param exampleInputPassword
     * @return
     */
    @PostMapping("/login")
    public ModelAndView login(@RequestParam("exampleInputEmail") String exampleInputEmail,
                              @RequestParam("exampleInputPassword") String exampleInputPassword,
                              @RequestParam("customCheck") String remember,
                              HttpSession session,
                              HttpServletRequest request,
                              HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView("login");
        UserAccInfo login = userLoginService.login(exampleInputEmail);
        if (login == null) {
            modelAndView.addObject("err","用户不存在");
            rabbitTemplate.convertAndSend("log.direct","info","login:一个不存在的用户登陆，email:" + exampleInputEmail);
        } else if (!login.getPassword().equals(exampleInputPassword)) {
            modelAndView.addObject("err","密码错误");
            rabbitTemplate.convertAndSend("log.direct","info","login:用户登陆密码错误,email:" + exampleInputEmail);
        } else {
            modelAndView = new ModelAndView("index");
            modelAndView.addObject("username",login.getName());
            modelAndView.addObject("id",login.getId());     //将用户id发送到index页面
            modelAndView.addObject("currentDir",fileOperationService.getFileById(-1*login.getId(), login.getId(),0)); //用户根文件夹id
            modelAndView.addObject("location","");  //位置
            List<FileInfo> all = fileOperationService.getFilesByFid(-1 * login.getId(), login.getId());    //所有文件
            getDirsAndDocs(modelAndView, all);
            ArrayList<FileInfo> fileInfos = new ArrayList<>();
            modelAndView.addObject("path",fileInfos);
            session.setAttribute("loginUserId",login.getId());
            session.setAttribute("loginUserName",login.getName());
            if (!"".equals(remember)) {     //记住我功能
                rememberMe(exampleInputEmail, exampleInputPassword, request, response);
            }
            rabbitTemplate.convertAndSend("log.direct","info","login:用户登陆成功，id为："+login.getId());
        }
        return modelAndView;
    }

    public void rememberMe(String exampleInputEmail, String exampleInputPassword, HttpServletRequest request, HttpServletResponse response) {
        Cookie cookieUserName = new Cookie("loginEmail", exampleInputEmail);
        Cookie cookieUserId = new Cookie("loginPassword",exampleInputPassword);
        cookieUserId.setMaxAge(7*24*60*60);
        cookieUserName.setMaxAge(7*24*60*60);
        cookieUserId.setPath(request.getContextPath());
        cookieUserName.setPath(request.getContextPath());
        response.addCookie(cookieUserId);
        response.addCookie(cookieUserName);
    }

    public static void getDirsAndDocs(ModelAndView modelAndView, List<FileInfo> all) {
        List<FileInfo> dir = new ArrayList<>();     //文件夹
        List<FileInfo> doc = new ArrayList<>();     //文档
        for (FileInfo fileInfo : all) {
            if (fileInfo.getType() == 0) {
                dir.add(new FileInfo(fileInfo));
            } else {
                doc.add(new FileInfo(fileInfo));
            }
        }
        modelAndView.addObject("dirs",dir);
        modelAndView.addObject("docs",doc);
    }

    /**
     * 忘记密码，给用户注册邮箱发送一个带有过期时间的链接修改密码
     * @param email
     * @return
     */
    @ResponseBody
    @RequestMapping("/forget_password")
    public String forget_password(String email) {
        //先检查邮箱
        if (!userLoginService.isExist(email)) {     //用户未注册
            return "notExist";
        }
        //生成uuid
        String uuid = UUID.randomUUID().toString();
        String link = "http://localhost:8080/pages/reset-password?uuid=" + uuid + "&email=" + email;
        String context = "<h1>Now, you can reset your password by clicking on the link below</h1><br><hr>" + link;
        Map<String,String> map = new HashMap<>(9);
        //发送邮件
        map.put("source_email",SOURCE_EMAIL_ADDRESS);
        map.put("dest_email",email);
        map.put("title","vfd-cloud");
        map.put("context",context);
        map.put("html","true");
        //加入uuid入redis
        map.put("key",email+":uuid");
        map.put("val",uuid);
        map.put("time","60");
        //记录日志
        map.put("log","forget_password:用户修改密码,email:" + email);
        rabbitTemplate.convertAndSend("account.topic","account.sendUUid",map);
        return "success";
    }

    /**
     * 处理重置密码的请求
     * @param exampleInputPassword
     * @param exampleRepeatPassword
     * @param email
     * @param request
     * @return
     */
    @RequestMapping("/reset_password")
    public ModelAndView reset_password(@RequestParam("exampleInputPassword") String exampleInputPassword,
                                       @RequestParam("exampleRepeatPassword") String exampleRepeatPassword,
                                       @RequestParam("email") String email,
                                       HttpServletRequest request){
        ModelAndView modelAndView = null;
        if (!exampleInputPassword.equals(exampleRepeatPassword)) {
            modelAndView = new ModelAndView("reset-password");
            modelAndView.addObject("err", "密码不一致");
            request.setAttribute("email",email);
        } else {
            if (email.length() == 0) {
                email = (String) request.getAttribute("email");
            }
            if (userLoginService.updateUserPassword(email,exampleInputPassword)) {
                Map<String, String> map = new HashMap<>(2);
                map.put("del",email+":uuid");
                map.put("log","reset_password:用户密码修改成功，email:" + email);
                rabbitTemplate.convertAndSend("account.topic","account.reset",map);
                modelAndView = new ModelAndView("login");
                modelAndView.addObject("msg","密码修改成功，请登录");
            } else {
                modelAndView = new ModelAndView("reset-password");
                modelAndView.addObject("err","密码修改失败，请重试");
                logger.info("reset_password:用户密码修改失败，email:" + email);
                request.setAttribute("email",email);
            }
        }
        return modelAndView;
    }

    @RequestMapping("/logout")
    public String logout (HttpSession session,
                          HttpServletRequest request,
                          HttpServletResponse response) {
        //销毁session存储的信息
        session.removeAttribute("loginUserId");
        session.removeAttribute("loginUserName");

        Cookie cookieUserName = new Cookie("loginEmail", "");
        Cookie cookieUserId = new Cookie("loginPassword","");
        cookieUserId.setMaxAge(0);
        cookieUserName.setMaxAge(0);
        cookieUserId.setPath(request.getContextPath());
        cookieUserName.setPath(request.getContextPath());
        response.addCookie(cookieUserId);
        response.addCookie(cookieUserName);
        return "login";
    }

    /**
     * 生成6位验证码
     * @return
     */
    private String getVerificationCode() {
        Random random = new Random();
        StringBuilder verificationCode = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            verificationCode.append(random.nextInt(10));
        }
        if (verificationCode.length() == 6) {
            return verificationCode.toString();
        } else {
            throw new VerificationCodeLengthException(verificationCode.length());
        }
    }
}