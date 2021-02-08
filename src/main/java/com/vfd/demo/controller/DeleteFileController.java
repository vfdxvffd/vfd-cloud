package com.vfd.demo.controller;

import com.vfd.demo.service.FileOperationService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    @Autowired
    RabbitTemplate rabbitTemplate;

    @ResponseBody
    @RequestMapping("delete")
    public String deleteFile (@RequestParam("id") Integer id,
                              @RequestParam("owner") Integer owner,
                              @RequestParam("fid") Integer fid) {
        //fileOperationService.deleteFileOnDiskById(id);
        if (fileOperationService.deleteFileById(id, owner,fid)) {
            rabbitTemplate.convertAndSend("log.direct","info","delete: 文件id为" + id + "的文件成功从数据库删除");
            return "success";
        } else {
            rabbitTemplate.convertAndSend("log.direct","error","delete: 文件id为" + id + "的文件从数据库删除失败");
            return "fail";
        }
    }
}