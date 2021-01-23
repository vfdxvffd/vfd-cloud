package com.vfd.demo.controller;

import com.vfd.demo.service.FileOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

    @ResponseBody
    @RequestMapping("delete")
    public String deleteFile (@RequestParam("id") Integer id) {
        fileOperationService.deleteFileOnDiskById(id);
        if (fileOperationService.deleteFileById(id)) {
            return "success";
        } else {
            return "fail";
        }
    }
}