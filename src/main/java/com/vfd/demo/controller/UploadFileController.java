package com.vfd.demo.controller;

import com.vfd.demo.bean.FileInfo;
import com.vfd.demo.service.FileOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    @PostMapping("/uploadFile")
    public ModelAndView uploadFile(@RequestPart("select_file")MultipartFile[] files,
                                   @RequestParam("username") String username,
                                   @RequestParam("fid") Integer fid,
                                   @RequestParam("location") String location) {
        ModelAndView modelAndView = new ModelAndView("forward:/enterFile");
        if (files.length > 0) {
            try {
                for (MultipartFile file:files) {
                    if (!file.isEmpty()) {
                        FileInfo fileInfo = new FileInfo(null,file.getOriginalFilename(), files.length, fid, location,1);
                        fileOperationService.saveFile(fileInfo);
                        boolean mkdir = new File("/home/vfdxvffd/vfd-cloud/" + fileInfo.getId()).mkdirs();
                        file.transferTo(new File("/home/vfdxvffd/vfd-cloud/"+ fileInfo.getId() + "/" + file.getOriginalFilename()));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        modelAndView.addObject("username",username);
        modelAndView.addObject("fid",fid); //文件id
        modelAndView.addObject("location",location);  //位置
        return modelAndView;
    }


    /**
     modelAndView.addObject("username",exampleInputEmail);
     modelAndView.addObject("id",login);     //将用户id发送到index页面
     modelAndView.addObject("fid",-1*login); //文件id
     modelAndView.addObject("location","allFiles");  //位置
     modelAndView.addObject("files",new File("/
     */
    //通过文件夹的id进入某个文件夹fid
    @PostMapping("/enterFile")
    public ModelAndView enterFile(@RequestParam("username") String username,
                                  @RequestParam("fid") Integer fid) {
        ModelAndView modelAndView = new ModelAndView("index");
        System.out.println("fid============" + fid);
        String location = fileOperationService.getLocationById(fid);
        modelAndView.addObject("username",username);
        modelAndView.addObject("fid",fid); //文件id
        modelAndView.addObject("location",location);  //位置
        List<FileInfo> filesByFid = fileOperationService.getFilesByFid(fid);
        modelAndView.addObject("files",filesByFid);
        String[] dirs = location.split(">");
        ArrayList<FileInfo> fileInfos = new ArrayList<>();
        for (String dir:dirs) {
            String[] split = dir.split("\\.");
            fileInfos.add(new FileInfo(Integer.parseInt(split[0]),split[1]));
        }
        modelAndView.addObject("path",fileInfos);
        return modelAndView;
    }
}