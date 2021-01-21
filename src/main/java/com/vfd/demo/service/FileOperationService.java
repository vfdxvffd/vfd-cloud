package com.vfd.demo.service;

import com.vfd.demo.bean.FileInfo;

/**
 * @PackageName: com.vfd.demo.service
 * @ClassName: FileOperation
 * @Description: 对文件的数据库操作
 * @author: vfdxvffd
 * @date: 2021/1/21 下午7:56
 */
public interface FileOperation {

    /**
     * 上传一个文件，将信息保存到数据库
     * @param fileInfo
     * @return
     */
    Boolean saveFile (FileInfo fileInfo);
}