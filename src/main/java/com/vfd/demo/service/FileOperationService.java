package com.vfd.demo.service;

import com.vfd.demo.bean.FileInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.IOException;
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
     * 带有id的保存信息
     * @param fileInfo
     * @return
     */
    Boolean saveFileFullInfo (FileInfo fileInfo);

    /**
     * 通过父目录id获取其下所有文件
     * @param fid
     * @return
     */
    List<FileInfo> getFilesByFid(Integer fid, Integer owner);

    /**
     * 通过id、owner、location获取文件对象
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!谨慎使用!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * @param id
     * @param owner
     * @param location
     * @return
     */
    FileInfo getFileByLocal(@Param("id") Integer id, @Param("owner") Integer owner, @Param("location") String location);

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

    /**
     * 根据location获取pid
     * @param location
     * @return
     */
    List<Integer> getPidByLocal(String location);

    /**
     * 将一个文件（夹）移动到回收站
     * @param fileInfo
     * @param fileInfos
     * @return
     */
    Boolean moveToTrash(FileInfo fileInfo, List<FileInfo> fileInfos);

    /**
     * 根据文件的主键动态更新信息
     * @param fileInfo
     * @return
     */
    Boolean updateFileInfo(FileInfo fileInfo);

    /**
     * get count by id
     * @param id
     * @return
     */
    Integer getCountById(Integer id);

    /**
     *
     * @param id
     * @param targetDir
     * @return
     */
    List<FileInfo> getPath(Integer id, FileInfo targetDir);

    /**
     *
     * @param fileInfo
     * @param result
     */
    void getAllSubFiles(FileInfo fileInfo, List<FileInfo> result);

    /**
     *
     * @param fileInfo
     * @param result
     * @param location
     * @param userId
     */
    void getAllSubFileInfo(FileInfo fileInfo, List<FileInfo> result, String location, Integer userId);

    /**
     *
     * @param fileInfo
     * @param name
     * @param owner
     * @return
     */
    int reNameDir(FileInfo fileInfo, String name, Integer owner);

    /**
     *
     * @param name
     * @param count
     * @return
     */
    String nameWithCount(String name, int count);

    /**
     *
     * @param source
     * @param dest
     * @throws IOException
     */
     void copyFileUsingFileChannels(File source, File dest) throws IOException;

    /**
     *
     * @param byFid
     * @param headmanFile
     * @param f
     * @return
     */
    String getNewName(List<FileInfo> byFid, FileInfo f);
}