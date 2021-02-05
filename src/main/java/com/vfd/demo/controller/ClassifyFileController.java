package com.vfd.demo.controller;

import com.vfd.demo.bean.FileInfo;
import com.vfd.demo.service.FileOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


import java.util.List;

/**
 * @PackageName: com.vfd.demo.controller
 * @ClassName: ClassifyFileController
 * @Description:
 * @author: vfdxvffd
 * @date: 2021/2/4 下午6:06
 */
@Controller
public class ClassifyFileController {

    @Autowired
    FileOperationService fileOperationService;

    @ResponseBody
    @RequestMapping("/find/document/{id}")
    public List<FileInfo> findAllDocuments(@PathVariable("id") Integer id) {
        return fileOperationService.getFilesByType(1,id);
    }

    @ResponseBody
    @RequestMapping("/find/picture/{id}")
    public List<FileInfo> findAllPictures(@PathVariable("id") Integer id) {
        return fileOperationService.getFilesByType(2,id);
    }

    @ResponseBody
    @RequestMapping("/find/video/{id}")
    public List<FileInfo> findAllVideos(@PathVariable("id") Integer id) {
        return fileOperationService.getFilesByType(3,id);
    }

    @ResponseBody
    @RequestMapping("/find/audio/{id}")
    public List<FileInfo> findAllAudios(@PathVariable("id") Integer id) {
        return fileOperationService.getFilesByType(4,id);
    }

    @ResponseBody
    @RequestMapping("/find/torrent/{id}")
    public List<FileInfo> findAllTorrents(@PathVariable("id") Integer id) {
        return fileOperationService.getFilesByType(5,id);
    }

    @ResponseBody
    @RequestMapping("/find/others/{id}")
    public List<FileInfo> findAllOthers(@PathVariable("id") Integer id) {
        return fileOperationService.getFilesByType(6,id);
    }
}