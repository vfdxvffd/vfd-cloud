package com.vfd.demo.service.impl;

import com.vfd.demo.bean.FileInfo;
import com.vfd.demo.mapper.FileOperationMapper;
import com.vfd.demo.service.FileOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
