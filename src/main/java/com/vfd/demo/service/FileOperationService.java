package com.vfd.demo.service;

import com.vfd.demo.bean.FileInfo;

import java.util.List;

/**
 * @PackageName: com.vfd.demo.service
 * @ClassName: FileOperation
 * @Description: 对文件的数据库操作
 * @author: vfdxvffd
 * @date: 2021/1/21 下午7:56
 */
public interface FileOperationService {

    /**
     * 上传一个文件，将信息保存到数据库
     * @param fileInfo
     * @return
     */
    Boolean saveFile (FileInfo fileInfo);

    /**
     * 通过父目录id获取其下所有文件
     * @param fid
     * @return
     */
    List<FileInfo> getFilesByFid(Integer fid);

    /**
     * 通过id获得location和名字
     * @param id
     * @return
     */
    FileInfo getFileById(Integer id);
}