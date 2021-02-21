package com.vfd.demo.controller;

import com.vfd.demo.bean.FileInfo;
import com.vfd.demo.bean.ShareInfo;
import com.vfd.demo.bean.UserAccInfo;
import com.vfd.demo.mapper.FileOperationMapper;
import com.vfd.demo.service.FileOperationService;
import com.vfd.demo.service.RedisService;
import com.vfd.demo.service.UserLoginService;
import com.vfd.demo.utils.MagicValue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.*;

/**
 * @PackageName: com.vfd.demo.controller
 * @ClassName: ShareFileController
 * @Description:
 * @author: vfdxvffd
 * @date: 2021/2/6 下午8:22
 */
@Controller
public class ShareFileController {

    @Autowired
    RedisService redisService;
    @Autowired
    FileOperationService fileOperationService;
    @Autowired
    UserLoginService userLoginService;
    @Autowired
    FileOperationMapper fileOperationMapper;
    @Autowired
    RabbitTemplate rabbitTemplate;

    @ResponseBody
    @RequestMapping("/getShareLink")
    public Map<String, String> getShareLink (@RequestParam("id") Integer id,
                                             @RequestParam("owner") Integer owner,
                                             @RequestParam("fid") Integer fid,
                                             @RequestParam("time") Integer time) {
        Map<String, String> result = new HashMap<>();
        StringBuilder pass = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            char c = (char)('a'+Math.random()*('z'-'a'+1));
            pass.append(c);
        }
        result.put("pass",new String(pass));
        UUID uuid = UUID.randomUUID();
        result.put("link", MagicValue.linkPrefix+"/pages/share-file?sharer=" + owner + "&uuid=" + uuid);
        //将待分享的文件信息和生成的提取码以及验证UUID存入缓存中，时间设定为time天
        Map<String,Object> info = new HashMap<>();
        info.put("pass",new String(pass));
        info.put("fileInfo", new FileInfo(id,fid,owner));
        String key = "shareFile:"+owner+":"+uuid;
        if (redisService.hmset(key,info,time*24*60*60)) {
            rabbitTemplate.convertAndSend("log.direct","info","分享链接生成成功:" + result);
            result.put("success","true");
        } else {
            rabbitTemplate.convertAndSend("log.direct","warn","分享链接生成失败:" + result);
            result.put("success","false");
        }
        return result;
    }

    //分享文件，保存文件

    @RequestMapping("/pages/share-file")
    public ModelAndView shareFile(String uuid, Integer sharer) {
        ModelAndView modelAndView = null;
        String key = "shareFile:"+sharer+":"+uuid;
        boolean hasKey = redisService.hasKey(key);
        if (hasKey) {
            modelAndView = new ModelAndView("share-file");
            modelAndView.addObject("uuid",uuid);
            modelAndView.addObject("sharer",sharer);
        } else {
            modelAndView = new ModelAndView("blank");   //来晚了，文件分享被取消了
        }
        return modelAndView;
    }

    @RequestMapping("/test")
    public ModelAndView test(HttpServletRequest request, HttpSession session) {
        //request.setAttribute("uuid","123");
        //request.setAttribute("pass","321");
        ModelAndView modelAndView = new ModelAndView("redirect:/preserve-file");
        modelAndView.addObject("uuid","123");
        modelAndView.addObject("pass","pass");
        session.setAttribute("loginUserId",1);
        session.setAttribute("loginUserName","hahaha");
        return modelAndView;
    }

    @RequestMapping("/preserve-file")
    public ModelAndView preserveFile (@RequestParam("uuid") String uuid,
                                      @RequestParam("pass") String pass,
                                      @RequestParam("sharer") Integer sharer,
                                      HttpServletRequest request,
                                      HttpSession session) {
        Integer userId = (Integer) session.getAttribute("loginUserId");
        String userName = (String) session.getAttribute("loginUserName");
        if (userId == null || userName == null) {
            //从Cookie获取
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                String userEmail = null;
                String userPassword = null;
                for (Cookie item:cookies) {
                    if ("loginEmail".equals(item.getName())) {
                        userEmail = item.getValue();
                    } else if ("loginPassword".equals(item.getName())) {
                        userPassword = item.getValue();
                    }
                }
                if (userEmail != null && userPassword != null) {
                    UserAccInfo login = userLoginService.login(userEmail);
                    System.out.println("login:"+login);
                    if (login != null && userPassword.equals(login.getPassword())) {
                        session.setAttribute("loginUserId",login.getId());
                        session.setAttribute("loginUserName",login.getName());
                        userId = login.getId();
                        userName = login.getName();
                    }
                }
            }
        }
        ModelAndView modelAndView = null;
        String key = "shareFile:"+sharer+":"+uuid;
        Map<Object, Object> info = redisService.hmget(key);
        long expire = redisService.getExpire(key);
        String realPass = (String) info.get("pass");
        if (realPass.equals(pass)) {
            //提取码正确
            modelAndView = new ModelAndView("keep-file");
            modelAndView.addObject("uuid",uuid);
            modelAndView.addObject("pass",pass);
            modelAndView.addObject("sharer",sharer);
            FileInfo fileInfo = (FileInfo) info.get("fileInfo");
            FileInfo file = fileOperationService.getFileById(fileInfo.getId(), fileInfo.getOwner(), fileInfo.getPid());
            modelAndView.addObject("file",file);
            if (userId != null) {
                modelAndView.addObject("id",userId);
                if (file.getOwner() == userId)      //自己打开自己分享的文件
                    modelAndView.addObject("self","true");
            }
            int day = 60*60*24;
            if (expire % day == 0)
                expire = expire/day;
            else
                expire = expire/day + 1;
            if (file.getType() == 0) {  //文件夹
                modelAndView.addObject("path",new ArrayList<>());
                modelAndView.addObject("currentDir",file);
                List<FileInfo> all = new ArrayList<>();
                List<Integer> pidByLocal = fileOperationService.getPidByLocal(file.getLocation() + ">" + file.getId() + "." + file.getName());
                if (pidByLocal.size() > 0)
                    all = fileOperationService.getFilesByFid(pidByLocal.get(0), file.getOwner());    //所有文件
                AccountController.getDirsAndDocs(modelAndView, all);
            } else {
                List<FileInfo> doc = new ArrayList<>();
                doc.add(file);
                modelAndView.addObject("docs",doc);
            }
            modelAndView.addObject("expire",expire);
            modelAndView.addObject("userName",userName);
            modelAndView.addObject("ownerName", userLoginService.getNameById(file.getOwner()));

        } else {
            //提取码错误
            modelAndView = new ModelAndView("share-file");
            modelAndView.addObject("uuid",uuid);
            modelAndView.addObject("err_msg","提取码错误");
        }
        return modelAndView;
    }

    @RequestMapping("/keep_file")
    public ModelAndView keepFile (HttpSession session,
                                  @RequestParam("id") Integer id,
                                  @RequestParam("fid") Integer fid,
                                  @RequestParam("owner") Integer owner,
                                  @RequestParam("targetDir") String target) {
        Integer userId = (Integer) session.getAttribute("loginUserId");
        String userName = (String) session.getAttribute("loginUserName");
        FileInfo fileInfo = fileOperationService.getFileById(id,owner,fid);
        String[] s = target.split("_");
        FileInfo targetDir = fileOperationService.getFileById(Integer.parseInt(s[0]),Integer.parseInt(s[1]),Integer.parseInt(s[2]));
        String local = fileInfo.getLocation() + ">" + fileInfo.getId() + "." + fileInfo.getName();
        String location = targetDir.getLocation() + ">" + targetDir.getId()+"." + targetDir.getName();
        fileInfo.setLocation(location);
        List<FileInfo> all = new ArrayList<>();
        List<Integer> pidByLocal = fileOperationService.getPidByLocal(location);
        if (pidByLocal.size() > 0) {
            fileInfo.setPid(pidByLocal.get(0));
        } else {
            fileInfo.setPid(null);
        }
        fileInfo.setTime(new Timestamp(new Date().getTime()));
        List<FileInfo> allSubFiles = new ArrayList<>();
        allSubFiles.add(fileInfo);
        getAllSubFileInfo(fileInfo, allSubFiles, local);
        fileOperationService.keepFiles(allSubFiles, userId);        //保存到数据库
        rabbitTemplate.convertAndSend("log.direct","info","分享的文件保存成功:" + userId +
                "fileInfo:" + fileInfo + "targetDir:" + targetDir);
        ModelAndView modelAndView = new ModelAndView("index");
        modelAndView.addObject("username",userName);
        modelAndView.addObject("id",userId);     //将用户id发送到index页面
        modelAndView.addObject("currentDir",targetDir); //用户根文件夹id
        modelAndView.addObject("location",targetDir.getLocation());  //位置
        if (pidByLocal.size() > 0) {
            all = fileOperationService.getFilesByFid(pidByLocal.get(0), userId);    //所有文件
        }
        AccountController.getDirsAndDocs(modelAndView, all);
        modelAndView.addObject("path",getPath(id, targetDir));
        return modelAndView;
    }

    public List<FileInfo> getPath(@RequestParam("id") Integer id, FileInfo targetDir) {
        String[] dirId = targetDir.getLocation().split(">");
        ArrayList<FileInfo> fileInfos = new ArrayList<>();
        StringBuilder local = new StringBuilder();
        for (String dir : dirId) {
            String[] split = dir.split("\\.");
            if (split.length == 2) {
                List<Integer> pre_id = fileOperationService.getPidByLocal(new String(local));
                if (pre_id.size() > 0) {
                    fileInfos.add(new FileInfo(Integer.parseInt(split[0]), split[1], id, pre_id.get(0)));
                    local.append(">").append(dir);
                }
                else {
                    System.out.println("err acc");
                }
            }
        }
        return fileInfos;
    }

    public void getAllSubFileInfo(FileInfo fileInfo, List<FileInfo> result, String location) {
        List<Integer> pidByLocal = fileOperationService.getPidByLocal(location);
        List<FileInfo> subFiles = new ArrayList<>();
        if (pidByLocal.size() > 0) {
            subFiles = fileOperationService.getFilesByFid(pidByLocal.get(0), fileInfo.getOwner());
        }
        for (FileInfo f:subFiles) {
            String local = f.getLocation() + ">" + f.getId() + "." + f.getName();
            f.setTime(new Timestamp(new Date().getTime()));
            f.setLocation(fileInfo.getLocation() + ">" + fileInfo.getId() + "." + fileInfo.getName());
            f.setPid(pidByLocal.get(0));        //设置pid
            result.add(f);
            if (f.getType() == 0) {
                getAllSubFileInfo(f,result,local);
            }
        }
    }

    @RequestMapping("/getAllLink")
    public ModelAndView getAllLink(HttpSession session) {
        Integer loginUserId = (Integer) session.getAttribute("loginUserId");
        Object loginUserName = session.getAttribute("loginUserName");
        List<String> key = redisService.getKey("shareFile:" + loginUserId + ":*");
        List<ShareInfo> shareInfos = new ArrayList<>();
        for (String k:key) {
            Map<Object, Object> hmget = redisService.hmget(k);
            String pass = (String) hmget.get("pass");
            String[] split = k.split(":");
            long expire = redisService.getExpire(k);
            shareInfos.add(new ShareInfo(MagicValue.linkPrefix+"/pages/share-file?sharer=" + loginUserId + "&uuid=" + split[2],
                    pass, split[2], expire));
        }
        ModelAndView modelAndView = new ModelAndView("share-link");
        modelAndView.addObject("share",shareInfos);
        modelAndView.addObject("userName",loginUserName);
        return modelAndView;
    }


    @ResponseBody
    @RequestMapping("/deleteShare")
    public String deleteShare(@RequestParam("uuid") String uuid, HttpSession session) {
        Integer loginUserId = (Integer) session.getAttribute("loginUserId");
        String key = "shareFile:" + loginUserId + ":" + uuid;
        redisService.del(key);
        rabbitTemplate.convertAndSend("log.direct","info","分享链接删除成功:" + key);
        return "success";
    }
}