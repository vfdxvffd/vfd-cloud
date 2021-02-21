package com.vfd.demo.controller;

import com.vfd.demo.bean.FileInfo;
import com.vfd.demo.bean.TrashInfo;
import com.vfd.demo.service.FileOperationService;
import com.vfd.demo.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

/**
 * @PackageName: com.vfd.demo.controller
 * @ClassName: TrashController
 * @Description:
 * @author: vfdxvffd
 * @date: 2021/2/19 下午10:33
 */
@Controller
public class TrashController {

    @Autowired
    RedisService redisService;
    @Autowired
    FileOperationService fileOperationService;

    Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/enterTrash")
    public ModelAndView enterTrash(HttpSession session,
                                   HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("trash");
        Integer loginUserId = (Integer) session.getAttribute("loginUserId");
        Object loginUserName = session.getAttribute("loginUserName");
        String key = "trash:" + loginUserId + ":*";
        List<String> keys = redisService.getKey(key);
        List<TrashInfo> trashInfos = new ArrayList<>();
        for (String k:keys) {
            Map<Object, Object> map = redisService.hmget(k);
            TrashInfo tmp = (TrashInfo) map.get("headman");
            tmp.setExpire(redisService.getExpire(k));
            trashInfos.add(tmp);
        }
        trashInfos.forEach(System.out::println);
        modelAndView.addObject("trashInfos",trashInfos);
        modelAndView.addObject("userName",loginUserName);
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping("deleteFromTrash")
    public String deleteFromTrash(HttpSession session,
                                  @RequestParam("id") Integer id,
                                  @RequestParam("fid") Integer fid) {
        Integer loginUserId = (Integer) session.getAttribute("loginUserId");
        String key = "trash:" + loginUserId + ":" + id + "_" + fid;
        if (redisService.hasKey(key)) {
            redisService.del(key);
        } else {
            return "fail";
        }
        return "success";
    }

    @ResponseBody
    @RequestMapping("/deleteAllFromTrash")
    public String deleteAllFromTrash(HttpSession session) {
        Integer loginUserId = (Integer) session.getAttribute("loginUserId");
        String key = "trash:" + loginUserId + ":*";
        List<String> keys = redisService.getKey(key);
        redisService.del(keys.toArray(new String[0]));
        return "success";
    }

    @ResponseBody
    @RequestMapping("/checkDuplicate")
    public Map<String, Object> checkDuplicate(HttpSession session,
                                              @RequestParam("id") Integer id,
                                              @RequestParam("owner") Integer owner,
                                              @RequestParam("fid") Integer fid,
                                              HttpServletRequest request,
                                              HttpServletResponse response) throws ServletException, IOException {
        Integer loginUserId = (Integer) session.getAttribute("loginUserId");
        Map<String, Object> result = new HashMap<>();
        result.put("id",id);
        result.put("owner",owner);
        result.put("fid",fid);
        if (!loginUserId.equals(owner)) {       //非本人操作
            request.getRequestDispatcher("/pages/login").forward(request,response);
            result.put("notSelf","true");
            return result;
        }
        String key = "trash:" + loginUserId + ":" + id + "_" + fid;
        Map<Object, Object> map = redisService.hmget(key);
        TrashInfo headman = (TrashInfo) map.get("headman");
        String location = headman.getLocation();
        String[] split = location.split(">");
        String local = "";
        for (int i = 1; i < split.length; i++) {        //for结束后就可以将headman前面的所有路径都处理完成
            String[] tmp = split[i].split("\\.");
            if (fileOperationService.getFileByLocal(Integer.parseInt(tmp[0]),loginUserId,local)==null) {    //没有该目录
                List<Integer> pidByLocal = fileOperationService.getPidByLocal(local);
                if (pidByLocal.size() > 0) {
                    List<FileInfo> filesByFid = fileOperationService.getFilesByFid(pidByLocal.get(0), loginUserId);
                    for (FileInfo fileInfo : filesByFid) {
                        if (fileInfo.getName().equals(tmp[1])) {
                            //发生重名冲突
                            result.put("duplicate",fileInfo);
                            return result;
                        }
                    }
                }
            }
            local += ">" + tmp[0] + "." + tmp[1];
        }
        FileInfo headmanFile = new FileInfo(headman);
        List<Integer> pidByLocal = fileOperationService.getPidByLocal(headmanFile.getLocation());
        if (pidByLocal.size() <= 0)
            return result;
        else {
            headmanFile.setPid(pidByLocal.get(0));
            List<FileInfo> byFid = fileOperationService.getFilesByFid(pidByLocal.get(0), loginUserId);
            for (FileInfo f : byFid) {
                if (f.getName().equals(headmanFile.getName())) {
                    result.put("duplicate",f);
                    return result;
                }
            }
        }
        return result;
    }

    @ResponseBody
    @RequestMapping("/resumeFile")
    public String resumeFileFromTrash(HttpSession session,
                                      @RequestParam("id") Integer id,
                                      @RequestParam("owner") Integer owner,
                                      @RequestParam("fid") Integer fid,
                                      @RequestParam("op") Boolean op) {
        Integer loginUserId = (Integer) session.getAttribute("loginUserId");
        if (!loginUserId.equals(owner)) {       //非本人操作
            return "notSelf";
        }
        String key = "trash:" + loginUserId + ":" + id + "_" + fid;
        Map<Object, Object> map = redisService.hmget(key);
        TrashInfo headman = (TrashInfo) map.get("headman");
        String location = headman.getLocation();
        String[] split = location.split(">");
        String local = "";
        for (int i = 1; i < split.length; i++) {        //for结束后就可以将headman前面的所有路径都处理完成
            FileInfo duplicate = null;
            String[] tmp = split[i].split("\\.");
            if (fileOperationService.getFileByLocal(Integer.parseInt(tmp[0]),loginUserId,local)==null) {    //没有该目录
                System.out.println(local);
                List<Integer> pidByLocal = fileOperationService.getPidByLocal(local);
                if (pidByLocal.size() > 0) {
                    List<FileInfo> filesByFid = fileOperationService.getFilesByFid(pidByLocal.get(0), loginUserId);
                    for (FileInfo fileInfo : filesByFid) {
                        if (fileInfo.getName().equals(tmp[1])) {
                            duplicate = fileInfo;
                            break;
                        }
                    }
                    if (duplicate != null) {
                        // 覆盖？ 重命名？ 取消
                        if (op) {       //覆盖
                            List<FileInfo> result = new ArrayList<>();
                            getAllSubFiles(duplicate,result);     //将待移动的文件（夹）及其子目录下所有的文件（夹）保存起来
                            System.out.println("覆盖,result = " + result);
                            result.forEach(System.out::println);
                            fileOperationService.moveToTrash(duplicate,result);
                            FileInfo fileInfo = new FileInfo(Integer.parseInt(tmp[0]),tmp[1],0L,null,local,0,new Timestamp(new Date().getTime()),loginUserId);
                            List<Integer> pidByLocal1 = fileOperationService.getPidByLocal(local);
                            if (pidByLocal1.size() > 0)
                                fileInfo.setPid(pidByLocal1.get(0));
                            else
                                fileInfo.setPid(null);
                            fileOperationService.saveFileFullInfo(fileInfo);
                        } else {        //共存，重命名
                            int count = 1;
                            while (true) {
                                boolean dup = false;
                                for (FileInfo fileInfo : filesByFid) {
                                    if (fileInfo.getName().equals(tmp[1]+"("+count+")")) {
                                        dup = true;
                                        break;
                                    }
                                }
                                if (!dup) {
                                    reNameDir(duplicate, tmp[1]+"("+count+")", loginUserId);
                                    FileInfo fileInfo = new FileInfo(Integer.parseInt(tmp[0]),tmp[1],0L,null,local,0,new Timestamp(new Date().getTime()),loginUserId);
                                    List<Integer> pidByLocal1 = fileOperationService.getPidByLocal(local);
                                    if (pidByLocal1.size() > 0)
                                        fileInfo.setPid(pidByLocal1.get(0));
                                    else
                                        fileInfo.setPid(null);
                                    fileOperationService.saveFileFullInfo(fileInfo);
                                    break;
                                }
                                count++;
                            }
                        }
                    } else {
                        //创建
                        FileInfo fileInfo = new FileInfo(Integer.parseInt(tmp[0]),tmp[1],0L,null,local,0,new Timestamp(new Date().getTime()),loginUserId);
                        List<Integer> pidByLocal1 = fileOperationService.getPidByLocal(local);
                        if (pidByLocal1.size() > 0)
                            fileInfo.setPid(pidByLocal1.get(0));
                        else
                            fileInfo.setPid(null);
                        fileOperationService.saveFileFullInfo(fileInfo);
                    }
                } else {
                    FileInfo fileInfo = new FileInfo(Integer.parseInt(tmp[0]),tmp[1],0L,null,local,0,new Timestamp(new Date().getTime()),loginUserId);
                    List<Integer> pidByLocal1 = fileOperationService.getPidByLocal(local);
                    if (pidByLocal1.size() > 0)
                        fileInfo.setPid(pidByLocal1.get(0));
                    else
                        fileInfo.setPid(null);
                    fileOperationService.saveFileFullInfo(fileInfo);
                }

            }
            local += ">" + tmp[0] + "." + tmp[1];
        }

        FileInfo headmanFile = new FileInfo(headman);
        List<Integer> pidByLocal = fileOperationService.getPidByLocal(headmanFile.getLocation());
        if (pidByLocal.size() <= 0)
            headmanFile.setPid(null);
        else {
            headmanFile.setPid(pidByLocal.get(0));
            List<FileInfo> byFid = fileOperationService.getFilesByFid(pidByLocal.get(0), loginUserId);
            for (FileInfo f : byFid) {
                if (f.getName().equals(headmanFile.getName())) {
                    if (op) {
                        List<FileInfo> result = new ArrayList<>();
                        getAllSubFiles(f,result);     //将待移动的文件（夹）及其子目录下所有的文件（夹）保存起来
                        fileOperationService.moveToTrash(f,result);
                    } else {
                        int count = 1;
                        while (true) {
                            boolean dup = false;
                            for (FileInfo fileInfo : byFid) {
                                if (fileInfo.getName().equals(headmanFile.getName()+"("+count+")")) {
                                    dup = true;
                                    break;
                                }
                            }
                            if (!dup) {
                                reNameDir(f, headmanFile.getName()+"("+count+")", loginUserId);
                                break;
                            }
                            count++;
                        }
                    }
                    break;
                }
            }
        }
        headmanFile.setTime(new Timestamp(new Date().getTime()));
        fileOperationService.saveFileFullInfo(headmanFile);
        List<FileInfo> subFiles = (List<FileInfo>) map.get("subFiles");
        System.out.println("size:" + subFiles.size());
        for (FileInfo file : subFiles) {
            List<Integer> pid = fileOperationService.getPidByLocal(file.getLocation());
            if (pid.size() > 0) {
                file.setPid(pid.get(0));
            } else {
                file.setPid(null);
            }
            file.setTime(new Timestamp(new Date().getTime()));
            fileOperationService.saveFileFullInfo(file);
        }
        redisService.del(key);
        return "success";
    }

    public void getAllSubFiles(FileInfo fileInfo, List<FileInfo> result) {
        List<Integer> pidByLocal = fileOperationService.getPidByLocal(fileInfo.getLocation()+">"+fileInfo.getId()+
                "."+fileInfo.getName());
        List<FileInfo> subFiles = new ArrayList<>();
        if (pidByLocal.size() > 0) {
            subFiles = fileOperationService.getFilesByFid(pidByLocal.get(0), fileInfo.getOwner());
        }
        for (FileInfo f:subFiles) {
            result.add(f);
            if (f.getType() == 0) {
                getAllSubFiles(f,result);
            }
        }
    }

    public void reNameDir(FileInfo fileInfo, String name, Integer owner) {
        List<Integer> pidByLocal = fileOperationService.getPidByLocal(fileInfo.getLocation()+">"+fileInfo.getId()+"."+fileInfo.getName());
        Integer pid = null;
        if (pidByLocal.size() > 0) {
            pid = pidByLocal.get(0);
        }
        System.out.println("pid = " + pid);
        System.out.println("fileInfo = " + fileInfo);
        System.out.println("delete = " + fileOperationService.deleteFileById(fileInfo.getId(), fileInfo.getOwner(), fileInfo.getPid()));
        fileInfo.setId(null);
        fileInfo.setName(name);
        Boolean saveFile = fileOperationService.saveFile(fileInfo);
        if (!saveFile) {
            logger.error("不该出现的命名重复:fileInfo" + fileInfo + "\nname:" + name);
            return;
        }
        if (pid != null) {
            updateSubFilesLocation(fileInfo, pid, owner);
        }

    }

    public void updateSubFilesLocation(FileInfo fileInfo, Integer pid, Integer owner) {
        List<FileInfo> filesByFid = fileOperationService.getFilesByFid(pid, owner);
        for (FileInfo info : filesByFid) {
            List<Integer> pidByLocal = fileOperationService.getPidByLocal(info.getLocation()+">"+info.getId()+"."+info.getName());
            FileInfo file = new FileInfo(info.getId(),info.getPid(),info.getOwner());
            String location = fileInfo.getLocation() + ">" + fileInfo.getId() + "." + fileInfo.getName();
            file.setLocation(location);
            fileOperationService.updateFileInfo(file);
            info.setLocation(location);
            if (pidByLocal.size() > 0) {
                updateSubFilesLocation(info, pidByLocal.get(0), owner);
            }
        }
    }
}