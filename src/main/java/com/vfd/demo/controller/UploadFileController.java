package com.vfd.demo.controller;

import com.vfd.demo.bean.FileInfo;
import com.vfd.demo.service.FileOperationService;
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
        map.put("text/html", 1);
        map.put("text/javascript", 1);
        map.put("text/css", 1);
        map.put("application/pdf", 1);
        map.put("application/vnd.ms-works", 1);
        map.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", 1);
        map.put("application/vnd.ms-powerpoint", 1);
        map.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", 1);
        map.put("application/msword", 1);
        map.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 1);
        map.put("application/vnd.ms-excel", 1);
        map.put("application/vnd.ms-htmlhelp", 1);
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
        //种子
        map.put("application/x-bittorrent", 5);
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
            try {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String mimeType = file.getContentType();
                        int type = 6;
                        if (map.containsKey(mimeType)) {
                            type = map.get(mimeType);
                        }
                        FileInfo fileInfo = new FileInfo(null, file.getOriginalFilename(), file.getSize(), fid, location+">"+fid+"."+fName, type, new Timestamp(new Date().getTime()), id);
                        Boolean saveFile = fileOperationService.saveFile(fileInfo);
                        if (!saveFile) {        //往数据库添加失败
                            fileInfo.setType(0);//如果往数据库添加失败就将这条记录的type改为0,返回前端提示出来
                            result.add(fileInfo);
                            continue;
                        }
                        result.add(fileInfo);
                        boolean mkdir = new File(PROJECT_DIR + fileInfo.getId()).mkdirs();
                        file.transferTo(new File(PROJECT_DIR + fileInfo.getId() + "/" + file.getOriginalFilename()));
                        fileOperationService.encryptFile(file.getOriginalFilename(), fileInfo.getId());
                        rabbitTemplate.convertAndSend("log.direct", "info", "upload: 文件上传成功，文件id为" + fileInfo.getId());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
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
        List<FileInfo> all = fileOperationService.getFilesByFid(fid, id);
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
        List<FileInfo> path = ShareFileController.getPath(id, fileInfo);
        result.put("path", path);
        return result;
    }


    @ResponseBody
    @PostMapping("/mkdir")
    public FileInfo mkdir(@RequestParam("id") Integer id,
                          @RequestParam("fid") Integer fid,
                          @RequestParam("location") String location,
                          @RequestParam("inputDir") String dirName,
                          @RequestParam("f_name") String fName,
                          HttpSession session) {
        FileInfo result = new FileInfo(dirName, 0L, fid, location + ">" + fid + "." + fName, 0, new Timestamp(new Date().getTime()), id);
        Boolean saveFile = fileOperationService.saveFile(result);
        if (saveFile) {
            return result;
        } else {
            FileInfo fail = new FileInfo();     //向数据库中插入文件夹信息失败
            fail.setId(0);
            return fail;
        }
    }
}