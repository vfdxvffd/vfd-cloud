package com.vfd.demo.service.impl;

import com.vfd.demo.bean.FileInfo;
import com.vfd.demo.mapper.FileOperationMapper;
import com.vfd.demo.service.FileOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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

    @Override
    public Boolean saveFile(FileInfo fileInfo) {
        return fileOperationMapper.saveFile(fileInfo);
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
        System.out.println("id = " + id + ":正在磁盘删除文件");
        File file = new File("/home/vfdxvffd/vfd-cloud/" + id);
        if (file.exists()) {
            boolean delete1 = Objects.requireNonNull(file.listFiles())[0].delete();
            boolean delete = file.delete();
            System.out.println("文件删除" + (delete1 && delete));
        }
    }
}
