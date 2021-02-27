package com.vfd.demo.controller;

import com.vfd.demo.bean.FileInfo;
import com.vfd.demo.bean.TrashInfo;
import com.vfd.demo.service.FileOperationService;
import com.vfd.demo.service.RedisService;
import com.vfd.demo.utils.MagicValue;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
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

    private Integer loginUserId;
    private String key;
    private Map<Object, Object> map;
    private TrashInfo headman;
    private String[] split;

    @RequestMapping("/enterTrash")
    public ModelAndView enterTrash(HttpSession session) {
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
        setValue(session,id,fid);
        Map<String, Object> result = new HashMap<>();
        result.put("id",id);
        result.put("owner",owner);
        result.put("fid",fid);
        if (!loginUserId.equals(owner)) {       //非本人操作
            request.getRequestDispatcher("/pages/login").forward(request,response);
            result.put("notSelf","true");
            return result;
        }
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
    @RequestMapping("/resumeFileWithNoDup")
    public String resumeFileFromTrashWithNoDup(HttpSession session,
                                      @RequestParam("id") Integer id,
                                      @RequestParam("owner") Integer owner,
                                      @RequestParam("fid") Integer fid) {
        setValue(session,id,fid);
        if (!loginUserId.equals(owner)) {       //非本人操作
            return "notSelf";
        }
        String local = "";
        for (int i = 1; i < split.length; i++) {        //for结束后就可以将headman前面的所有路径都处理完成
            String[] tmp = split[i].split("\\.");
            if (fileOperationService.getFileByLocal(Integer.parseInt(tmp[0]),loginUserId,local)==null) {    //没有该目录
                FileInfo fileInfo = new FileInfo(Integer.parseInt(tmp[0]),tmp[1],0L,null,local,0,new Timestamp(new Date().getTime()),loginUserId);
                List<Integer> pidByLocal1 = fileOperationService.getPidByLocal(local);
                if (pidByLocal1.size() > 0)
                    fileInfo.setPid(pidByLocal1.get(0));
                else
                    fileInfo.setPid(null);
                fileOperationService.saveFileFullInfo(fileInfo);
            }
            local += ">" + tmp[0] + "." + tmp[1];
        }

        FileInfo headmanFile = new FileInfo(headman);
        List<Integer> pidByLocal = fileOperationService.getPidByLocal(headmanFile.getLocation());
        if (pidByLocal.size() <= 0)
            headmanFile.setPid(null);
        else
            headmanFile.setPid(pidByLocal.get(0));
        return saveHeadmanAndSubFiles(headmanFile);
    }

    @ResponseBody
    @RequestMapping("/resumeFileWithCover")
    public String resumeFileFromTrashWithCover(HttpSession session,
                                      @RequestParam("id") Integer id,
                                      @RequestParam("owner") Integer owner,
                                      @RequestParam("fid") Integer fid) {
        setValue(session,id,fid);
        if (!loginUserId.equals(owner)) {       //非本人操作
            return "notSelf";
        }
        String local = "";
        for (int i = 1; i < split.length; i++) {        //for结束后就可以将headman前面的所有路径都处理完成
            String[] tmp = split[i].split("\\.");
            if (fileOperationService.getFileByLocal(Integer.parseInt(tmp[0]),loginUserId,local)==null) {    //没有该目录
                //创建目录之前必须解决冲突  覆盖
                List<Integer> pidByLocal = fileOperationService.getPidByLocal(local);
                if (pidByLocal.size() > 0) {        //如果小于0,说明此目录下没有文件，则可以直接创建
                    List<FileInfo> filesByFid = fileOperationService.getFilesByFid(pidByLocal.get(0), loginUserId);
                    for (FileInfo f : filesByFid) {     //for结束之后可以将path中的第i个目录冲突解决完成
                        if (f.getName().equals(tmp[1])) {   //删除f及以下所有内容
                            List<FileInfo> result = new ArrayList<>();
                            fileOperationService.getAllSubFiles(f,result);     //将待移动的文件（夹）及其子目录下所有的文件（夹）保存起来
                            fileOperationService.moveToTrash(f,result);
                            break;
                        }
                    }
                }
                //创建
                FileInfo fileInfo = new FileInfo(Integer.parseInt(tmp[0]),tmp[1],0L,null,local,0,new Timestamp(new Date().getTime()),loginUserId);
                List<Integer> pidByLocal1 = fileOperationService.getPidByLocal(local);
                if (pidByLocal1.size() > 0)
                    fileInfo.setPid(pidByLocal1.get(0));
                else
                    fileInfo.setPid(null);
                fileOperationService.saveFileFullInfo(fileInfo);
            }
            local += ">" + tmp[0] + "." + tmp[1];
        }

        //处理headman
        FileInfo headmanFile = new FileInfo(headman);
        List<Integer> pidByLocal = fileOperationService.getPidByLocal(headmanFile.getLocation());
        if (pidByLocal.size() <= 0)     //保存headman的目录下没有任何文件
            headmanFile.setPid(null);
        else {
            headmanFile.setPid(pidByLocal.get(0));
            fileOperationService.coverDupFile(pidByLocal, loginUserId, headmanFile);
        }
        return saveHeadmanAndSubFiles(headmanFile);
    }

    @ResponseBody
    @RequestMapping("/resumeFileWithRename")
    public String resumeFileFromTrashWithRename(HttpSession session,
                                               @RequestParam("id") Integer id,
                                               @RequestParam("owner") Integer owner,
                                               @RequestParam("fid") Integer fid) {
        setValue(session,id,fid);
        if (!loginUserId.equals(owner)) {       //非本人操作
            return "notSelf";
        }
        String local = "";
        for (int i = 1; i < split.length; i++) {        //for结束后就可以将headman前面的所有路径都处理完成
            String[] tmp = split[i].split("\\.");
            if (fileOperationService.getFileByLocal(Integer.parseInt(tmp[0]),loginUserId,local)==null) {    //没有该目录
                //创建目录之前必须解决冲突  重命名
                List<Integer> pidByLocal = fileOperationService.getPidByLocal(local);
                if (pidByLocal.size() > 0) {        //如果小于0,说明此目录下没有文件，则可以直接创建
                    List<FileInfo> filesByFid = fileOperationService.getFilesByFid(pidByLocal.get(0), loginUserId);
                    for (FileInfo f : filesByFid) {     //for结束之后可以将path中的第i个目录冲突解决完成
                        if (f.getName().equals(tmp[1])) {   //重命名f及修改以下内容的location
                            String newName = fileOperationService.getNewName(filesByFid,f);
                            fileOperationService.reNameDir(f,newName,loginUserId);
                            break;
                        }
                    }
                }
                //创建
                FileInfo fileInfo = new FileInfo(Integer.parseInt(tmp[0]),tmp[1],0L,null,local,0,new Timestamp(new Date().getTime()),loginUserId);
                List<Integer> pidByLocal1 = fileOperationService.getPidByLocal(local);
                if (pidByLocal1.size() > 0)
                    fileInfo.setPid(pidByLocal1.get(0));
                else
                    fileInfo.setPid(null);
                fileOperationService.saveFileFullInfo(fileInfo);
            }
            local += ">" + tmp[0] + "." + tmp[1];
        }

        //处理headman
        FileInfo headmanFile = new FileInfo(headman);
        List<Integer> pidByLocal = fileOperationService.getPidByLocal(headmanFile.getLocation());
        if (pidByLocal.size() <= 0)     //保存headman的目录下没有任何文件
            headmanFile.setPid(null);
        else {
            headmanFile.setPid(pidByLocal.get(0));
            List<FileInfo> byFid = fileOperationService.getFilesByFid(pidByLocal.get(0), loginUserId);
            for (FileInfo f : byFid) {      //检查headman同目录下是否有命名冲突
                if (f.getName().equals(headmanFile.getName())) {    //将冲突的文件重命名，并修改其子目录的location
                    String newName = fileOperationService.getNewName(byFid,f);
                    Integer fileId = f.getId();
                    String name = f.getName();
                    int i = fileOperationService.reNameDir(f, newName, loginUserId);
                    if (f.getType() > 0) {
                        Integer countById = fileOperationService.getCountById(fileId);
                        if (countById > 0) {
                            //有多处引用此id的文件    另外复制一份来重命名
                            File file = new File(MagicValue.fileAddress + "/" + i);
                            boolean b = file.mkdirs();
                            File sourceFile = new File(MagicValue.fileAddress + "/" + fileId + "/" + name);
                            File destFile = new File(MagicValue.fileAddress + "/" + i + "/" + newName);
                            try {
                                fileOperationService.copyFileUsingFileChannels(sourceFile, destFile);
                            } catch (IOException e) {
                                return "exception";
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
        return saveHeadmanAndSubFiles(headmanFile);
    }

    private void setValue(HttpSession session, Integer id, Integer fid) {
        loginUserId = (Integer) session.getAttribute("loginUserId");
        key = "trash:" + loginUserId + ":" + id + "_" + fid;
        map = redisService.hmget(key);
        headman = (TrashInfo) map.get("headman");
        String location = headman.getLocation();
        split = location.split(">");
    }

    private String saveHeadmanAndSubFiles(FileInfo headmanFile) {
        //创建headman的数据库中的记录
        headmanFile.setTime(new Timestamp(new Date().getTime()));
        fileOperationService.saveFileFullInfo(headmanFile);
        //创建headman的子目录在数据库中的记录
        List<FileInfo> subFiles = (List<FileInfo>) map.get("subFiles");
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
}