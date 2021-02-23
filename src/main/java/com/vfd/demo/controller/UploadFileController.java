package com.vfd.demo.controller;

import com.vfd.demo.bean.FileInfo;
import com.vfd.demo.service.FileOperationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

/**
 * @PackageName: com.vfd.demo.controller
 * @ClassName: UploadFileController
 * @Description:
 * @author: vfdxvffd
 * @date: 2021/1/21 下午3:48
 */
@Controller
public class UploadFileController {

    public static String PROJECT_DIR = "/home/vfdxvffd/vfd-cloud/";
    public static Map<String, Integer> map = new HashMap<>();

    static {
        //文档
        map.put("text/markdown", 1);
        map.put("text/plain", 1);
        map.put("application/pdf", 1);
        map.put("application/vnd.ms-works", 1);
        map.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", 1);
        map.put("application/vnd.ms-powerpoint", 1);
        map.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", 1);
        map.put("application/msword", 1);
        map.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 1);
        map.put("application/vnd.ms-excel", 1);
        map.put("application/vnd.ms-htmlhelp", 1);
        map.put("", 1);
        //图片
        map.put("image/png", 2);
        map.put("image/x-icon", 2);
        map.put("image/gif", 2);
        map.put("image/jpeg", 2);
        map.put("image/bmp", 2);
        map.put("image/webp", 2);
        map.put("image/vnd", 2);
        map.put("image/vnd.microsoft.icon", 2);
        map.put("image/svg+xml", 2);
        //视频
        map.put("video/x-msvideo", 3);
        map.put("video/jpm", 3);
        map.put("video/jpeg", 3);
        map.put("video/quicktime", 3);
        map.put("video/x-sgi-movie", 3);
        map.put("video/mp4", 3);
        map.put("video/mpeg", 3);
        map.put("video/ogg", 3);
        //音频
        map.put("audio/mpeg", 4);
        map.put("audio/mp4", 4);
        map.put("audio/ogg", 4);
        //代码
        map.put("text/x-c", 5);
        map.put("application/java", 5);
        map.put("text/css", 5);
        map.put("text/html", 5);
        map.put("text/javascript", 5);
        map.put("application/java-archive", 5);
        map.put("application/xml", 5);
        map.put("application/x-sql", 5);
        map.put("application/json", 5);
        map.put("text/x-java-source", 5);
    }


    @Autowired
    FileOperationService fileOperationService;
    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 上传文件
     *
     * @param files
     * @param username
     * @param fid
     * @param location
     * @return
     */
    @ResponseBody
    @PostMapping("/uploadFile")
    public List<FileInfo> uploadFile(@RequestPart("select_file") MultipartFile[] files,
                                     @RequestParam("id") Integer id,
                                     @RequestParam("fid") Integer fid,
                                     @RequestParam("fName") String fName,
                                     @RequestParam("location") String location) {
        List<FileInfo> result = new ArrayList<>();
        if (files.length > 0) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String mimeType = file.getContentType();
                    int type = 6;
                    if (map.containsKey(mimeType)) {
                        type = map.get(mimeType);
                    }
                    String local = location+">"+fid+"."+fName;
                    List<Integer> pid = fileOperationService.getPidByLocal(local);
                    FileInfo fileInfo = null;
                    if (pid.size() > 0) {
                        fileInfo = new FileInfo(null, file.getOriginalFilename(), file.getSize(), pid.get(0), local, type, new Timestamp(new Date().getTime()), id);
                    } else {
                        fileInfo = new FileInfo(null, file.getOriginalFilename(), file.getSize(), null, local, type, new Timestamp(new Date().getTime()), id);
                    }
                    Boolean saveFile = fileOperationService.saveFile(fileInfo);
                    if (!saveFile) {        //往数据库添加失败
                        rabbitTemplate.convertAndSend("log.direct","warn","文件上传插入数据库失败:" + fileInfo);
                        fileInfo.setType(0);//如果往数据库添加失败就将这条记录的type改为0,返回前端提示出来
                        result.add(fileInfo);
                        continue;
                    }
                    File dir = new File(PROJECT_DIR + fileInfo.getId());
                    boolean mkdir = dir.mkdirs();
                    try {
                        file.transferTo(new File(PROJECT_DIR + fileInfo.getId() + "/" + file.getOriginalFilename()));
                    } catch (Exception e) {
                        if (mkdir) {
                            fileOperationService.deleteDir(dir);
                        }
                        if (pid.size() > 0) {
                            rabbitTemplate.convertAndSend("log.direct","warn","文件上传写入磁盘失败:" + fileInfo);
                            fileOperationService.deleteFileById(fileInfo.getId(), fileInfo.getOwner(), fileInfo.getPid());
                        } else {
                            FileInfo fileByLocal = fileOperationService.getFileByLocal(fileInfo.getId(), fileInfo.getOwner(), fileInfo.getLocation());
                            rabbitTemplate.convertAndSend("log.direct","warn","文件上传写入磁盘失败:" + fileByLocal);
                            fileOperationService.deleteFileById(fileByLocal.getId(), fileByLocal.getOwner(), fileByLocal.getPid());
                        }
                        fileInfo.setType(7);//如果往磁盘写失败就将这条记录的type改为7,返回前端提示出来
                        result.add(fileInfo);
                        continue;
                    }
                    if (pid.size() > 0) {
                        result.add(fileInfo);
                    } else {
                        result.add(fileOperationService.getFileByLocal(fileInfo.getId(), fileInfo.getOwner(), fileInfo.getLocation()));
                    }
                    fileOperationService.encryptFile(file.getOriginalFilename(), fileInfo.getId());
                    rabbitTemplate.convertAndSend("log.direct", "info", "upload: 文件上传成功，文件id为" + fileInfo.getId());
                }
            }
        }
        return result;
    }


    /**
     * 通过文件夹的id进入某个文件夹fid
     * @param id 用户id
     * @param fid 要进入的目录id
     * @param pid 要进入目录的父目录id
     * @return
     */
    @ResponseBody
    @RequestMapping("/enterFile")
    public Map<String, Object> enterFile(@RequestParam("id") Integer id,
                                         @RequestParam("fid") Integer fid,
                                         @RequestParam("pid") Integer pid) {
        Map<String, Object> result = new HashMap<>();
        FileInfo fileInfo = fileOperationService.getFileById(fid, id, pid);  //父目录对象
        List<Integer> pidByLocal = fileOperationService.getPidByLocal(fileInfo.getLocation()+">"+fileInfo.getId()+"."+fileInfo.getName());
        List<FileInfo> all = new ArrayList<>();
        if (pidByLocal.size() > 0) {        //如果size=0说明没有一个文件的location是父目录对象
            all = fileOperationService.getFilesByFid(pidByLocal.get(0), id);
        }
        List<FileInfo> dirs = new ArrayList<>();     //文件夹
        List<FileInfo> docs = new ArrayList<>();     //文档
        for (FileInfo f : all) {
            if (f.getType() == 0) {
                dirs.add(new FileInfo(f));
            } else {
                docs.add(new FileInfo(f));
            }
        }
        result.put("dirs", dirs);
        result.put("docs", docs);
        result.put("currentDir", fileInfo);
        List<FileInfo> path = getPath(id, fileInfo);
        result.put("path", path);
        return result;
    }


    @ResponseBody
    @PostMapping("/mkdir")
    public FileInfo mkdir(@RequestParam("id") Integer id,
                          @RequestParam("fid") Integer fid,
                          @RequestParam("location") String location,
                          @RequestParam("inputDir") String dirName,
                          @RequestParam("f_name") String fName) {
        String local = location + ">" + fid + "." + fName;
        List<Integer> pid = fileOperationService.getPidByLocal(local);
        FileInfo result = null;
        if (pid.size() > 0) {
            result = new FileInfo(dirName, 0L, pid.get(0), local, 0, new Timestamp(new Date().getTime()), id);
            if (fileOperationService.saveFile(result)) {
                rabbitTemplate.convertAndSend("log.direct","info","文件夹创建成功:" + result);
                return result;
            } else {
                rabbitTemplate.convertAndSend("log.direct","warn","文件夹创建失败:" + result);
                FileInfo fail = new FileInfo();     //向数据库中插入文件夹信息失败
                fail.setId(0);
                return fail;
            }
        }
        else {
            result = new FileInfo(dirName, 0L, null, local, 0, new Timestamp(new Date().getTime()), id);
            if (fileOperationService.saveFile(result)) {
                FileInfo fileByLocal = fileOperationService.getFileByLocal(result.getId(), id, local);
                rabbitTemplate.convertAndSend("log.direct","info","文件夹创建成功:" + fileByLocal);
                return fileByLocal;
            } else {
                rabbitTemplate.convertAndSend("log.direct","warn","文件夹创建失败:" + result);
                FileInfo fail = new FileInfo();     //向数据库中插入文件夹信息失败
                fail.setId(0);
                return fail;
            }
        }
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
}