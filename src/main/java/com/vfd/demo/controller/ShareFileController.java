package com.vfd.demo.controller;

import com.vfd.demo.bean.FileInfo;
import com.vfd.demo.mapper.FileOperationMapper;
import com.vfd.demo.service.FileOperationService;
import com.vfd.demo.service.RedisService;
import com.vfd.demo.service.UserLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        result.put("link","http://localhost:8080/pages/share-file?uuid=" + uuid);
        //将待分享的文件信息和生成的提取码以及验证UUID存入缓存中，时间设定为time天
        Map<String,Object> info = new HashMap<>();
        info.put("pass",new String(pass));
        info.put("fileInfo", new FileInfo(id,fid,owner));
        redisService.hmset("shareFile:"+uuid,info,time*24*60*60);
        return result;
    }

    //分享文件，保存文件

    @RequestMapping("/pages/share-file")
    public ModelAndView shareFile(String uuid) {
        ModelAndView modelAndView = null;
        boolean hasKey = redisService.hasKey("shareFile:"+uuid);
        if (hasKey) {
            modelAndView = new ModelAndView("share-file");
            modelAndView.addObject("uuid",uuid);
        } else {
            modelAndView = new ModelAndView("blank");
        }
        return modelAndView;
    }

    @RequestMapping("/preserve-file")
    public ModelAndView preserveFile (@RequestParam("uuid") String uuid,
                              @RequestParam("pass") String pass) {
        ModelAndView modelAndView = null;
        Map<Object, Object> info = redisService.hmget("shareFile:" + uuid);
        String realPass = (String) info.get("pass");
        if (realPass.equals(pass)) {
            //提取码正确
            modelAndView = new ModelAndView("keep-file");
            FileInfo fileInfo = (FileInfo) info.get("fileInfo");
            FileInfo file = fileOperationService.getFileById(fileInfo.getId(), fileInfo.getOwner(), fileInfo.getPid());
            modelAndView.addObject("file",file);
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
                                  @RequestParam("owner") Integer owner) {
        Integer userId = (Integer) session.getAttribute("loginUserId");
        String userName = (String) session.getAttribute("loginUserName");
        if (userId == null || userName == null) {
            return new ModelAndView("redirect:/");
        }
        FileInfo fileInfo = new FileInfo(fileOperationService.getFileById(id,owner,fid));
        FileInfo targetDir = fileOperationService.getFileById(12,2,-2);
        fileInfo.setLocation(targetDir.getLocation() + ">" + targetDir.getId()+"." + targetDir.getName());
        fileInfo.setOwner(userId);
        fileInfo.setPid(targetDir.getId());
        fileInfo.setTime(new Timestamp(new Date().getTime()));
        fileOperationMapper.mkDir(fileInfo);
        ModelAndView modelAndView = new ModelAndView("forward:/enterFile");
        modelAndView.addObject("id",userId);
        modelAndView.addObject("fid",targetDir.getId());
        modelAndView.addObject("pid",targetDir.getPid());
        return modelAndView;
    }
}