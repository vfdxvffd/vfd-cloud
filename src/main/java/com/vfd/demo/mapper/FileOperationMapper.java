package com.vfd.demo.mapper;

import com.vfd.demo.bean.FileInfo;
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
     * 通过文件类型查找某个用户的所有文件
     * @param type
     * @param owner
     * @return
     */
    List<FileInfo> getFilesByType(@Param("type") Integer type, @Param("owner") Integer owner);
}