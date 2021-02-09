package com.vfd.demo.service;

import com.vfd.demo.bean.FileInfo;

import java.io.File;
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
    List<FileInfo> getFilesByFid(Integer fid, Integer owner);

    /**
     * 通过id获得location和名字
     * @param id
     * @return
     */
    FileInfo getFileById(Integer id, Integer owner, Integer fid);

    /**
     * 通过id删除某个文件
     * @param id
     */
    Boolean deleteFileById(Integer id, Integer owner, Integer fid);

    /**
     * 真正在磁盘上删除文件
     * @param id
     */
    void deleteFileOnDiskById(Integer id);

    /**
     * 删除一个文件夹
     * @param dir
     */
    void deleteDir(File dir);

    /**
     * 下载完文件后删除临时文件
     * @param dir
     */
    void deleteTmpDir(File dir);

    /**
     * 采用国密sm4算法加密文件
     * @param filePath
     * @param id
     */
    void encryptFile (String fileName, Integer id);

    /**
     * 采用国密sm4算法解密文件
     * @param filePath
     */
    String decryptFile (String fileName, Integer id);

    /**
     * 根据用户id和文件类型获取文件
     * @param type
     * @param owner
     * @return
     */
    List<FileInfo> getFilesByType (Integer type, Integer owner);

    /**
     * 分享文件夹的时候将文件夹及其所有子文件（夹）保存到数据库中
     * @param fileInfos
     * @return
     */
    Boolean keepFiles (List<FileInfo> fileInfos, Integer owner);
}