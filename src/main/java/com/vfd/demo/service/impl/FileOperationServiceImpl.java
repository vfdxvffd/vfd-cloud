package com.vfd.demo.service.impl;

import com.vfd.demo.bean.FileInfo;
import com.vfd.demo.controller.UploadFileController;
import com.vfd.demo.mapper.FileOperationMapper;
import com.vfd.demo.service.FileOperationService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;
import java.util.Objects;

/**
 * @PackageName: com.vfd.demo.service.impl
 * @ClassName: FileOperationServiceImpl
 * @Description:
 * @author: vfdxvffd
 * @date: 2021/1/21 下午8:49
 */
@Service
public class FileOperationServiceImpl implements FileOperationService {

    @Autowired
    FileOperationMapper fileOperationMapper;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public Boolean saveFile(FileInfo fileInfo) {
        List<FileInfo> filesByFid = getFilesByFid(fileInfo.getPid());
        for (FileInfo f:filesByFid) {
            if (f.getName().equals(fileInfo.getName())) {
                return false;
            }
        }
        Boolean saveFile = fileOperationMapper.saveFile(fileInfo);
        if (saveFile) {
            return true;
        } else {
            rabbitTemplate.convertAndSend("log.direct","error",this.getClass()+":文件信息录入数据库发生错误:" + fileInfo);
            return false;
        }
    }

    @Override
    public List<FileInfo> getFilesByFid(Integer fid) {
        return fileOperationMapper.getFilesByFid(fid);
    }

    @Override
    public FileInfo getFileById(Integer id) {
        return fileOperationMapper.getFileById(id);
    }

    @Override
    public Boolean deleteFileById(Integer id) {
        return fileOperationMapper.deleteFileById(id);
    }

    @Async
    @Override
    public void deleteFileOnDiskById(Integer id) {
        File file = new File(UploadFileController.PROJECT_DIR + id);
        if (file.exists()) {
            boolean delete1 = Objects.requireNonNull(file.listFiles())[0].delete();
            boolean delete = file.delete();
            if (!(delete1 && delete)) {
                rabbitTemplate.convertAndSend("log.direct","error","deleteFileOnDiskById:文件从硬盘删除发生错误:文件id为:" + id);
            }
        }
    }
}
