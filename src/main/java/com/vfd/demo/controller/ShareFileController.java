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
                                      @RequestParam("pass") String pass,
                                      HttpSession session) {
        Integer userId = (Integer) session.getAttribute("loginUserId");
        String userName = (String) session.getAttribute("loginUserName");
        ModelAndView modelAndView = null;
        Map<Object, Object> info = redisService.hmget("shareFile:" + uuid);
        long expire = redisService.getExpire("shareFile:" + uuid);
        String realPass = (String) info.get("pass");
        if (realPass.equals(pass)) {
            //提取码正确
            modelAndView = new ModelAndView("keep-file");
            FileInfo fileInfo = (FileInfo) info.get("fileInfo");
            FileInfo file = fileOperationService.getFileById(fileInfo.getId(), fileInfo.getOwner(), fileInfo.getPid());
            modelAndView.addObject("file",file);
            int day = 60*60*24;
            if (expire % day == 0)
                expire = expire/day;
            else
                expire = expire/day + 1;
            if (file.getType() == 0) {  //文件夹
                modelAndView.addObject("path",new ArrayList<>());
                modelAndView.addObject("currentDir",file);
                List<FileInfo> all = fileOperationService.getFilesByFid(file.getId(), file.getOwner());    //所有文件
                AccountController.getDirsAndDocs(modelAndView, all);
            } else {
                List<FileInfo> doc = new ArrayList<>();
                doc.add(file);
                modelAndView.addObject("docs",doc);
            }
            modelAndView.addObject("expire",expire);
            modelAndView.addObject("id",userId);
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
        if (userId == null || userName == null) {
            return new ModelAndView("redirect:/");
        }
        FileInfo fileInfo = fileOperationService.getFileById(id,owner,fid);
        String[] s = target.split("_");
        FileInfo targetDir = fileOperationService.getFileById(Integer.parseInt(s[0]),Integer.parseInt(s[1]),Integer.parseInt(s[2]));
        fileInfo.setLocation(targetDir.getLocation() + ">" + targetDir.getId()+"." + targetDir.getName());
        fileInfo.setPid(targetDir.getId());
        fileInfo.setTime(new Timestamp(new Date().getTime()));
        List<FileInfo> allSubFiles = new ArrayList<>();
        allSubFiles.add(fileInfo);
        getAllSubFileInfo(fileInfo,allSubFiles);
        fileOperationService.keepFiles(allSubFiles, userId);
        ModelAndView modelAndView = new ModelAndView("index");
        modelAndView.addObject("username",userName);
        modelAndView.addObject("id",userId);     //将用户id发送到index页面
        modelAndView.addObject("currentDir",targetDir); //用户根文件夹id
        modelAndView.addObject("location",targetDir.getLocation());  //位置
        List<FileInfo> all = fileOperationService.getFilesByFid(targetDir.getId(), userId);    //所有文件
        AccountController.getDirsAndDocs(modelAndView, all);
        modelAndView.addObject("path",getPath(id, targetDir));
        return modelAndView;
    }

    public static List<FileInfo> getPath(@RequestParam("id") Integer id, FileInfo targetDir) {
        String[] dirId = targetDir.getLocation().split(">");
        ArrayList<FileInfo> fileInfos = new ArrayList<>();
        int pre_id = 0;
        for (String dir : dirId) {
            String[] split = dir.split("\\.");
            if (split.length == 2) {
                fileInfos.add(new FileInfo(Integer.parseInt(split[0]), split[1], id, pre_id));
                pre_id = Integer.parseInt(split[0]);
            }
        }
        return fileInfos;
    }

    public void getAllSubFileInfo(FileInfo fileInfo, List<FileInfo> result) {
        List<FileInfo> subFiles = fileOperationService.getFilesByFid(fileInfo.getId(), fileInfo.getOwner());
        for (FileInfo f:subFiles) {
            f.setTime(new Timestamp(new Date().getTime()));
            f.setLocation(fileInfo.getLocation() + ">" + fileInfo.getId() + "." + fileInfo.getName());
            result.add(f);
            if (f.getType() == 0) {
                getAllSubFileInfo(f,result);
            }
        }
    }
}