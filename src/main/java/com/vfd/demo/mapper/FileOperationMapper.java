package com.vfd.demo.mapper;

import com.vfd.demo.bean.FileInfo;
import com.vfd.demo.bean.TrashInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @PackageName: com.vfd.demo.mapper
 * @ClassName: FileOperationMapper
 * @Description:
 * @author: vfdxvffd
 * @date: 2021/1/21 下午8:03
 */
@Mapper
@Repository
public interface FileOperationMapper {

    /**
     * 上传一个文件，将信息保存到数据库
     * @param fileInfo
     * @return
     */
    Boolean saveFile (FileInfo fileInfo);

    /**
     * 批量保存文件，需指明每个文件的所有信息
     * @param allFiles
     * @return
     */
    Boolean saveFiles (@Param("allFiles") List<FileInfo> allFiles, @Param("owner") Integer owner);

    /**
     * 创建文件夹
     * @param fileInfo
     * @return
     */
    Boolean mkDir(FileInfo fileInfo);

    /**
     * 通过父目录id获取其下所有文件
     * @param fid
     * @return
     */
    List<FileInfo> getFilesByFid(@Param("fid") Integer fid, @Param("owner") Integer owner);

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
     * 通过id获得文件对象
     * @param id
     * @return
     */
    FileInfo getFileById(@Param("id") Integer id, @Param("owner") Integer owner, @Param("fid") Integer fid);

    /**
     * 通过id删除某个文件
     * @param id
     */
    Boolean deleteFileById(@Param("id") Integer id, @Param("owner") Integer owner, @Param("fid") Integer fid);

    /**
     * 通过id删除某个文件
     * @param id
     */
    Boolean deleteFilesById(@Param("fileInfos") List<FileInfo> fileInfos);

    /**
     * 通过文件类型查找某个用户的所有文件
     * @param type
     * @param owner
     * @return
     */
    List<FileInfo> getFilesByType(@Param("type") Integer type, @Param("owner") Integer owner);

    /**
     * 根据location获取pid
     * @param location
     * @return
     */
    List<Integer> getPidByLocal(String location);

    /**
     * 向trash表加入信息
     * @return
     */
    Boolean moveToTrash(TrashInfo trashInfo);

    /**
     * 更新某个字段值
     * @param trashInfo
     * @return
     */
    Boolean updateTrashInfo (TrashInfo trashInfo);

    /**
     * 批量插入多条数据
     * @param trashInfos
     * @return
     */
    Boolean moveToTrashMul(@Param("fileInfos") List<FileInfo> fileInfos, @Param("headman") Integer headman);

    /**
     * 从回收站删除某条数据
     * @param flag
     * @return
     */
    Boolean deleteFromTrashByFlag(@Param("flag") Integer flag);

    /**
     * 从回收站删除某条数据
     * @param headman
     * @return
     */
    Boolean deleteFromTrashByHeadman(@Param("headman") Integer headman);

    /**
     * 根据headman查找信息
     * @param headman
     * @return
     */
    List<TrashInfo> getTrashInfosByHeadman(@Param("headman") Integer headman);

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
    List<Integer> getCountById(Integer id);
}