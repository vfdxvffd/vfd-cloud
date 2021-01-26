package com.vfd.demo.controller;

import com.vfd.demo.bean.FileInfo;
import com.vfd.demo.service.FileOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
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

    @Autowired
    FileOperationService fileOperationService;

    /**
     * 上传文件
     * @param files
     * @param username
     * @param fid
     * @param location
     * @return
     */
    @ResponseBody
    @PostMapping("/uploadFile")
    public List<FileInfo> uploadFile(@RequestPart("select_file")MultipartFile[] files,
                                     @RequestParam("username") String username,
                                     @RequestParam("fid") Integer fid,
                                     @RequestParam("location") String location) {
//        ModelAndView modelAndView = new ModelAndView("forward:/enterFile");
        List<FileInfo> result = new ArrayList<>();
        if (files.length > 0) {
            try {
                for (MultipartFile file:files) {
                    if (!file.isEmpty()) {
                        FileInfo fileInfo = new FileInfo(null,file.getOriginalFilename(), file.getSize(), fid, location,1);
                        Boolean saveFile = fileOperationService.saveFile(fileInfo);
                        if (!saveFile) {        //往数据库添加失败
                            fileInfo.setType(0);//如果往数据库添加失败就将这条记录的type改为0,返回前端提示出来
                            result.add(fileInfo);
                            continue;
                        }
                        result.add(fileInfo);
                        boolean mkdir = new File("/home/vfdxvffd/vfd-cloud/" + fileInfo.getId()).mkdirs();
                        file.transferTo(new File("/home/vfdxvffd/vfd-cloud/"+ fileInfo.getId() + "/" + file.getOriginalFilename()));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
//        modelAndView.addObject("username",username);
//        modelAndView.addObject("fid",fid); //文件id
//        //modelAndView.addObject("location",location);  //位置
//        return modelAndView;
    }


    /**
     modelAndView.addObject("username",exampleInputEmail);
     modelAndView.addObject("id",login);     //将用户id发送到index页面
     modelAndView.addObject("fid",-1*login); //文件id
     modelAndView.addObject("location","allFiles");  //位置
     modelAndView.addObject("files",new File("/
     */
    //通过文件夹的id进入某个文件夹fid
    @ResponseBody
    @PostMapping("/enterFile")
    public Map<String, Object> enterFile(@RequestParam("username") String username,
                                  @RequestParam("fid") Integer fid) {
        Map<String,Object> result = new HashMap<>();
        ModelAndView modelAndView = new ModelAndView("index");
        FileInfo fileInfo = fileOperationService.getFileById(fid);  //父目录对象
        modelAndView.addObject("username",username);
//        modelAndView.addObject("fid",fid); //文件id
//        modelAndView.addObject("location",fileInfo.getLocation());  //位置
        List<FileInfo> all = fileOperationService.getFilesByFid(fid);
        List<FileInfo> dirs = new ArrayList<>();     //文件夹
        List<FileInfo> docs = new ArrayList<>();     //文档
        for (FileInfo f : all) {
            if (f.getType() == 0) {
                dirs.add(new FileInfo(f));
            } else {
                docs.add(new FileInfo(f));
            }
        }
        modelAndView.addObject("dirs",dirs);
        modelAndView.addObject("docs",docs);
        result.put("dirs",dirs);
        result.put("docs",docs);
        String[] dirId = fileInfo.getLocation().split(">");
        ArrayList<FileInfo> fileInfos = new ArrayList<>();
        for (String dir:dirId) {
            String[] split = dir.split("\\.");
            if (split.length == 2)
                fileInfos.add(new FileInfo(Integer.parseInt(split[0]),split[1]));
        }
//        fileInfos.add(fileInfo);
        modelAndView.addObject("currentDir",fileInfo);
        modelAndView.addObject("path",fileInfos);
        result.put("currentDir",fileInfo);
        result.put("path",fileInfos);
//        return modelAndView;
        return result;
    }


    @ResponseBody
    @PostMapping("/mkdir")
    public FileInfo mkdir(@RequestParam("username") String username,
                              @RequestParam("fid") Integer fid,
                              @RequestParam("location") String location,
                              @RequestParam("inputDir") String dirName,
                              @RequestParam("f_name") String fName) {
//        ModelAndView modelAndView = new ModelAndView("forward:/enterFile");
        FileInfo result = new FileInfo(dirName, 0L, fid, location + ">" + fid + "." + fName, 0);
        Boolean saveFile = fileOperationService.saveFile(result);
        if (saveFile) {
            return result;
        } else {
            FileInfo fail = new FileInfo();     //向数据库中插入文件夹信息失败
            fail.setId(0);
            return fail;
        }
//        modelAndView.addObject("username",username);
//        modelAndView.addObject("fid",fid);
//        return modelAndView;
    }
}