package com.vfd.demo.controller;

import com.vfd.demo.bean.FileInfo;
import com.vfd.demo.bean.ShareInfo;
import com.vfd.demo.bean.UserAccInfo;
import com.vfd.demo.service.FileOperationService;
import com.vfd.demo.service.RedisService;
import com.vfd.demo.service.UserLoginService;
import com.vfd.demo.utils.MagicValue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
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
    RabbitTemplate rabbitTemplate;

    private Integer userId;
    private String userName;
    private FileInfo fileInfo;
    private FileInfo targetDir;
    private String local;
    private String location;

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
        //info.put("fileInfo", new FileInfo(id,name,owner,fid));
        info.put("fileInfo", fileOperationService.getFileById(id, owner, fid));
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
            Map<Object, Object> info = redisService.hmget(key);
            FileInfo fileInfo = (FileInfo) info.get("fileInfo");
            FileInfo fileById = fileOperationService.getFileById(fileInfo.getId(), fileInfo.getOwner(), fileInfo.getPid());
            if (fileById == null) {
                return new ModelAndView("new");
            }
            modelAndView = new ModelAndView("share-file");
            modelAndView.addObject("uuid",uuid);
            modelAndView.addObject("sharer",sharer);
        } else {
            modelAndView = new ModelAndView("new");   //来晚了，文件分享被取消了
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

    @ResponseBody
    @RequestMapping("/checkShareDuplicate")
    public Map<String, Object> checkShareDuplicate(HttpSession session,
                                                   @RequestParam("id") Integer id,
                                                   @RequestParam("fid") Integer fid,
                                                   @RequestParam("owner") Integer owner,
                                                   @RequestParam("targetDir") String target) {
        Map<String, Object> map = new HashMap<>();
        Integer userId = (Integer) session.getAttribute("loginUserId");
        String[] s = target.split("_");
        //目标文件夹
        FileInfo targetDir = fileOperationService.getFileById(Integer.parseInt(s[0]),Integer.parseInt(s[1]),Integer.parseInt(s[2]));
        String location = targetDir.getLocation() + ">" + targetDir.getId()+"." + targetDir.getName();
        List<Integer> pidByLocal = fileOperationService.getPidByLocal(location);
        if (pidByLocal.size() > 0) {
            FileInfo fileInfo = fileOperationService.getFileById(id,owner,fid);     //要保存的文件
            List<FileInfo> filesByFid = fileOperationService.getFilesByFid(pidByLocal.get(0), userId);
            for (FileInfo f:filesByFid) {
                if (f.getId().equals(fileInfo.getId())) {
                    map.put("dup","true");
                    return map;
                }
                if (f.getName().equals(fileInfo.getName())) {
                    map.put("self","true");
                    map.put("exist",f);
                    map.put("keep",fileInfo);
                    return map;
                }
            }
        }
        return map;
    }

    public void setValue(HttpSession session,
                         Integer id, Integer fid, Integer owner, String target) {
        userId = (Integer) session.getAttribute("loginUserId");
        userName = (String) session.getAttribute("loginUserName");
        fileInfo = fileOperationService.getFileById(id,owner,fid);
        String[] s = target.split("_");
        targetDir = fileOperationService.getFileById(Integer.parseInt(s[0]),Integer.parseInt(s[1]),Integer.parseInt(s[2]));
        local = fileInfo.getLocation() + ">" + fileInfo.getId() + "." + fileInfo.getName();
        location = targetDir.getLocation() + ">" + targetDir.getId()+"." + targetDir.getName();
    }

    @RequestMapping("/keep_file")
    public ModelAndView keepFile (HttpSession session,
                                  @RequestParam("id") Integer id,
                                  @RequestParam("fid") Integer fid,
                                  @RequestParam("owner") Integer owner,
                                  @RequestParam("targetDir") String target) {
        setValue(session,id,fid,owner,target);
        return saveShareFile();
    }

    @RequestMapping("/keepFileWithCover")
    public ModelAndView keepFileWithCover(HttpSession session,
                                          @RequestParam("id") Integer id,
                                          @RequestParam("fid") Integer fid,
                                          @RequestParam("owner") Integer owner,
                                          @RequestParam("targetDir") String target) {
        setValue(session,id,fid,owner,target);
        List<Integer> pidByLocal = fileOperationService.getPidByLocal(location);
        if (pidByLocal.size() > 0) {
            fileOperationService.coverDupFile(pidByLocal, userId, fileInfo);
        }
        return saveShareFile();
    }

    @RequestMapping("/keepFileWithRename")
    public ModelAndView keepFileWithRename(HttpSession session,
                                          @RequestParam("id") Integer id,
                                          @RequestParam("fid") Integer fid,
                                          @RequestParam("owner") Integer owner,
                                          @RequestParam("targetDir") String target) {
        setValue(session,id,fid,owner,target);
        List<Integer> pidByLocal = fileOperationService.getPidByLocal(location);
        if (pidByLocal.size() > 0) {
            List<FileInfo> filesByFid = fileOperationService.getFilesByFid(pidByLocal.get(0), userId);
            for (FileInfo f:filesByFid) {
                if (f.getName().equals(fileInfo.getName())) {   //重命名，即重命名f及修改f以下的子文件（夹）的location
                    String newName = fileOperationService.getNewName(filesByFid,f);
                    Integer fileId = f.getId();
                    String name = f.getName();
                    int i = fileOperationService.reNameDir(f, newName, userId);
                    if (f.getType() > 0) {
                        Integer countById = fileOperationService.getCountById(fileId);
                        if (countById > 0) {
                            //有多处引用此id的文件    另外复制一份来重命名
                            File file = new File(MagicValue.fileAddress + "/" + i);
                            boolean b = file.mkdirs();
                            File sourceFile = new File(MagicValue.fileAddress + "/" + fileId + "/" + name);
                            File destFile = new File(MagicValue.fileAddress + "/" + i + "/" + newName);
                            try {
                                fileOperationService.copyFileUsingFileChannels(sourceFile,destFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            //仅有一处引用此id文件就是f, 可以直接重命名
                            File file = new File(MagicValue.fileAddress + "/" + fileId);
                            boolean b = file.renameTo(new File(MagicValue.fileAddress + "/" + i));
                            file = new File(MagicValue.fileAddress + "/" + i + "/" + name);
                            boolean b1 = file.renameTo(new File(MagicValue.fileAddress + "/" + i + "/" + newName));
                        }
                    }
                    break;
                }
            }
        }
        return saveShareFile();
    }

    public ModelAndView saveShareFile() {
        List<Integer> pidByLocal;
        fileInfo.setLocation(location);
        pidByLocal = fileOperationService.getPidByLocal(location);
        if (pidByLocal.size() > 0) {
            fileInfo.setPid(pidByLocal.get(0));
        } else {
            fileInfo.setPid(null);
        }
        fileInfo.setTime(new Timestamp(new Date().getTime()));
        List<FileInfo> allSubFiles = new ArrayList<>();
        allSubFiles.add(fileInfo);
        fileOperationService.getAllSubFileInfo(fileInfo, allSubFiles, local, userId);
        fileOperationService.keepFiles(allSubFiles, userId);        //保存到数据库
        rabbitTemplate.convertAndSend("log.direct","info","分享的文件保存成功:" + userId +
                "fileInfo:" + fileInfo + "targetDir:" + targetDir);
        ModelAndView modelAndView = new ModelAndView("index");
        modelAndView.addObject("username",userName);
        modelAndView.addObject("id",userId);     //将用户id发送到index页面
        modelAndView.addObject("currentDir",targetDir); //用户根文件夹id
        modelAndView.addObject("location",targetDir.getLocation());  //位置
        pidByLocal = fileOperationService.getPidByLocal(location);
        List<FileInfo> all = new ArrayList<>();
        if (pidByLocal.size() > 0) {
            all = fileOperationService.getFilesByFid(pidByLocal.get(0), userId);    //所有文件
        }
        AccountController.getDirsAndDocs(modelAndView, all);
        modelAndView.addObject("path",fileOperationService.getPath(userId, targetDir));
        return modelAndView;
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
            FileInfo f = (FileInfo) hmget.get("fileInfo");
            String[] split = k.split(":");
            long expire = redisService.getExpire(k);
            shareInfos.add(new ShareInfo(MagicValue.linkPrefix+"/pages/share-file?sharer=" + loginUserId + "&uuid=" + split[2],
                    pass, split[2], expire, f.getName(),f.getType()));
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

    @ResponseBody
    @RequestMapping("/deleteAllShare")
    public String deleteAllShare(HttpSession session) {
        Integer loginUserId = (Integer) session.getAttribute("loginUserId");
        String key = "shareFile:" + loginUserId + ":*";
        redisService.del(redisService.getKey(key).toArray(new String[0]));
        rabbitTemplate.convertAndSend("log.direct", "info", "用户所有分享链接删除成功:" + loginUserId);
        return "success";
    }
}