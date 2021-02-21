package com.vfd.demo.controller;

import com.vfd.demo.bean.FileInfo;
import com.vfd.demo.bean.TrashInfo;
import com.vfd.demo.service.FileOperationService;
import com.vfd.demo.service.RedisService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Timestamp;
import java.util.*;

/**
 * @PackageName: com.vfd.demo.controller
 * @ClassName: DeleteFileController
 * @Description:
 * @author: vfdxvffd
 * @date: 2021/1/23 下午8:42
 */
@Controller
public class DeleteFileController {

    @Autowired
    FileOperationService fileOperationService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    RedisService redisService;

    @ResponseBody
    @RequestMapping("/delete")
    public String deleteFile (@RequestParam("id") Integer id,
                              @RequestParam("owner") Integer owner,
                              @RequestParam("fid") Integer fid) {
        if (fileOperationService.deleteFileById(id, owner,fid)) {
            rabbitTemplate.convertAndSend("log.direct","info","delete: 文件id为" + id + "的文件成功从数据库删除");
            return "success";
        } else {
            rabbitTemplate.convertAndSend("log.direct","error","delete: 文件id为" + id + "的文件从数据库删除失败");
            return "fail";
        }
    }

    @ResponseBody
    @RequestMapping("/moveToTrash")
    public String moveToTrash (@RequestParam("id") Integer id,
                               @RequestParam("owner") Integer owner,
                               @RequestParam("fid") Integer fid) {
        FileInfo dir = fileOperationService.getFileById(id, owner, fid);
        List<FileInfo> result = new ArrayList<>();
        getAllSubFiles(dir,result);     //将待移动的文件（夹）及其子目录下所有的文件（夹）保存起来
        Map<String, Object> map = new HashMap<>(2);
        TrashInfo trashInfo = new TrashInfo(dir,new Timestamp(new Date().getTime()),0L);
        map.put("headman",trashInfo);
        map.put("subFiles",result);
        String key = "trash:" + owner + ":" + id + "_" + fid;
        boolean hmset = redisService.hmset(key, map, 15 * 24 * 60 * 60);
        Boolean aBoolean = fileOperationService.moveToTrash(dir, result);
        if (hmset && aBoolean) {
            return "success";
        } else {
            return "fail";
        }
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
}