package com.vfd.demo.mapper;

import com.vfd.demo.bean.FileInfo;
import org.apache.ibatis.annotations.Mapper;
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
     * 通过父目录id获取其下所有文件
     * @param fid
     * @return
     */
    List<FileInfo> getFilesByFid(Integer fid);

    /**
     * 通过id获得location
     * @param id
     * @return
     */
    String getLocationById(Integer id);
}