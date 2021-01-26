package com.vfd.demo.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

    @RequestMapping("/download/{id}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable("id") Integer id) throws IOException {
        System.out.println(id);
        //读取文件
        File file = Objects.requireNonNull(new File("/home/vfdxvffd/vfd-cloud/" + id).listFiles())[0];
        String fileName = file.getName();
        //设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        String string = new String(fileName.getBytes("gbk"),"iso8859-1");
        headers.add("Content-Disposition", "attachment;filename=" + string);
        headers.add("Content-Length", String.valueOf(file.length()));
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new InputStreamResource(new FileInputStream(file)));
    }
}