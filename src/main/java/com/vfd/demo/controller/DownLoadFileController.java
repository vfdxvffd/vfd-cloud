package com.vfd.demo.controller;

import com.vfd.demo.service.FileOperationService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Objects;

/**
 * @PackageName: com.vfd.demo.controller
 * @ClassName: DownLoadFileController
 * @Description:
 * @author: vfdxvffd
 * @date: 2021/1/23 下午12:18
 */
@Controller
public class DownLoadFileController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    FileOperationService fileOperationService;


/*    @RequestMapping("/download/{id}/{userId}")
    public void downloadFile(@PathVariable("id") Integer id,
                             @PathVariable("userId") Integer userId,
                             HttpServletResponse response) throws IOException {
        String name = fileOperationService.getFileById(id, userId).getName();
        String uuid = fileOperationService.decryptFile(name, id);
        File file = Objects.requireNonNull(new File(UploadFileController.PROJECT_DIR + id + "/" + uuid).listFiles())[0];
        String fileName = file.getName();
        response.setContentLengthLong(file.length());
        response.setContentType("application/octet-stream");
        String string = new String(fileName.getBytes("gbk"),"iso8859-1");
        response.setHeader("Content-Disposition", "attachment;filename=" + string);
        byte[] buff = new byte[1024];
        //创建缓冲输入流
        BufferedInputStream bis = null;
        OutputStream outputStream = null;

        try {
            outputStream = response.getOutputStream();
            //这个路径为待下载文件的路径
            bis = new BufferedInputStream(new FileInputStream(file));
            System.out.println(file.delete());
            int read = bis.read(buff);

            //通过while循环写入到指定了的文件夹中
            while (read != -1) {
                outputStream.write(buff, 0, buff.length);
                outputStream.flush();
                read = bis.read(buff);
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }*/

    @RequestMapping("/download/{id}/{userId}/{fid}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable("id") Integer id,
                                                            @PathVariable("userId") Integer userId,
                                                            @PathVariable("fid") Integer fid) throws IOException {
        //读取文件
        String name = fileOperationService.getFileById(id, userId, fid).getName();
        String uuid = fileOperationService.decryptFile(name, id);
        File tmpDir = new File(UploadFileController.PROJECT_DIR + id + "/" + uuid);
        File file = Objects.requireNonNull(tmpDir.listFiles())[0];
        String fileName = file.getName();
        //设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        String string = new String(fileName.getBytes("gbk"),"iso8859-1");
        headers.add("Content-Disposition", "attachment;filename=" + string);
        headers.add("Content-Length", String.valueOf(file.length()));
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        rabbitTemplate.convertAndSend("log.direct","info","download: 文件id为 " + id + "的文件下载");
        ResponseEntity<InputStreamResource> body = ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new InputStreamResource(new FileInputStream(file)));
        //对临时解密的文件夹进行递归删除
        fileOperationService.deleteTmpDir(tmpDir);
        return body;
    }
}