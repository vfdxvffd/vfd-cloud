package com.vfd.demo.service.impl;

import com.vfd.demo.bean.FileInfo;
import com.vfd.demo.bean.TrashInfo;
import com.vfd.demo.controller.UploadFileController;
import com.vfd.demo.mapper.FileOperationMapper;
import com.vfd.demo.service.FileOperationService;
import com.vfd.demo.service.RedisService;
import com.vfd.demo.utils.Sm4Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.Timestamp;
import java.util.*;

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

    Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    @Transactional
    public Boolean saveFile(FileInfo fileInfo) {
        List<FileInfo> filesByFid = getFilesByFid(fileInfo.getPid(), fileInfo.getOwner());
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

    public Boolean saveFileFullInfo (FileInfo fileInfo) {
        return fileOperationMapper.mkDir(fileInfo);
    }

    @Override
    public List<FileInfo> getFilesByFid(Integer fid, Integer owner) {
        return fileOperationMapper.getFilesByFid(fid, owner);
    }

    @Override
    public FileInfo getFileByLocal(Integer id, Integer owner, String location) {
        return fileOperationMapper.getFileByLocal(id, owner, location);
    }

    @Override
    public FileInfo getFileById(Integer id, Integer owner, Integer fid) {
        return fileOperationMapper.getFileById(id, owner, fid);
    }

    @Override
    public Boolean deleteFileById(Integer id,Integer owner, Integer fid) {
        return fileOperationMapper.deleteFileById(id, owner, fid);
    }

    @Async
    @Override
    public void deleteFileOnDiskById(Integer id) {
        File file = new File(UploadFileController.PROJECT_DIR + id);
        if (file.exists()) {
            deleteDir(file);
        }
    }

    @Override
    public void deleteDir(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f:files) {
                if (f.isFile() || f.listFiles() == null) {
                    f.delete();
                } else {
                    deleteDir(f);
                }
            }
        }
        dir.delete();
    }

    @Async
    @Override
    public void deleteTmpDir(File dir) {
        deleteDir(dir);
    }

    @Async
    @Override
    public void encryptFile (String fileName, Integer id) {
        String tmp = UploadFileController.PROJECT_DIR + id + "/~tmp";
        String filePath = UploadFileController.PROJECT_DIR + id + "/" + fileName;
        Sm4Utils.encryptFile("86C63180C2806ED1F47B859DE501215B",filePath,tmp);
        File file = new File(filePath);
        boolean delete = file.delete();
        if (delete) {
            File tmpFile = new File(tmp);
            boolean b = tmpFile.renameTo(file);
        }
    }

    @Override
    public String decryptFile(String fileName, Integer id) {
        String uuid = UUID.randomUUID().toString();
        String tmp = UploadFileController.PROJECT_DIR + id + "/" + uuid;
        File dir = new File(tmp);
        if (!dir.exists()) {
            boolean mkdirs = dir.mkdirs();
        }
        String decrypt = UploadFileController.PROJECT_DIR + id + "/" + uuid +"/" + fileName;
        String source = UploadFileController.PROJECT_DIR + id + "/" + fileName;
        Sm4Utils.decryptFile("86C63180C2806ED1F47B859DE501215B",source,decrypt);
        return uuid;
    }

    @Override
    public List<FileInfo> getFilesByType(Integer type, Integer owner) {
        return fileOperationMapper.getFilesByType(type, owner);
    }

    @Override
    @Transactional
    public Boolean keepFiles(List<FileInfo> fileInfos, Integer owner) {
        return fileOperationMapper.saveFiles(fileInfos, owner);
    }

    @Override
    public List<Integer> getPidByLocal(String location) {
        return fileOperationMapper.getPidByLocal(location);
    }

    @Transactional
    @Override
    public Boolean moveToTrash(FileInfo fileInfo, List<FileInfo> fileInfos) {
        fileInfos.add(fileInfo);
        return fileOperationMapper.deleteFilesById(fileInfos);
    }

    @Override
    public Boolean updateFileInfo(FileInfo fileInfo) {
        return fileOperationMapper.updateFileInfo(fileInfo);
    }

    @Override
    public Integer getCountById(Integer id) {
        return fileOperationMapper.getCountById(id).size();
    }

    @Override
    public List<FileInfo> getPath(Integer id, FileInfo targetDir) {
        String[] dirId = targetDir.getLocation().split(">");
        ArrayList<FileInfo> fileInfos = new ArrayList<>();
        StringBuilder local = new StringBuilder();
        for (String dir : dirId) {
            String[] split = dir.split("\\.");
            if (split.length == 2) {
                List<Integer> pre_id = getPidByLocal(new String(local));
                if (pre_id.size() > 0) {
                    fileInfos.add(new FileInfo(Integer.parseInt(split[0]), split[1], id, pre_id.get(0)));
                    local.append(">").append(dir);
                }
                else {
                    System.out.println("err acc");
                }
            }
        }
        return fileInfos;
    }

    @Override
    public void getAllSubFiles(FileInfo fileInfo, List<FileInfo> result) {
        List<Integer> pidByLocal = getPidByLocal(fileInfo.getLocation()+">"+fileInfo.getId()+
                "."+fileInfo.getName());
        List<FileInfo> subFiles = new ArrayList<>();
        if (pidByLocal.size() > 0) {
            subFiles = getFilesByFid(pidByLocal.get(0), fileInfo.getOwner());
        }
        for (FileInfo f:subFiles) {
            result.add(f);
            if (f.getType() == 0) {
                getAllSubFiles(f,result);
            }
        }
    }

    @Override
    public void getAllSubFileInfo(FileInfo fileInfo, List<FileInfo> result, String location, Integer userId) {
        List<Integer> pidByLocal = getPidByLocal(location);
        List<FileInfo> subFiles = new ArrayList<>();
        if (pidByLocal.size() > 0) {
            subFiles = getFilesByFid(pidByLocal.get(0), fileInfo.getOwner());
        }
        Timestamp timestamp = new Timestamp(new Date().getTime());
        String local = fileInfo.getLocation() + ">" + fileInfo.getId() + "." + fileInfo.getName();
        for (int i = 0; i < subFiles.size(); i++) {
            FileInfo file = subFiles.get(i);
            String l = file.getLocation() + ">" + file.getId() + "." + file.getName();
            file.setTime(timestamp);
            file.setLocation(local);
            if (i == 0) {
                int owner = file.getOwner();
                file.setOwner(userId);
                List<Integer> pid = getPidByLocal(local);
                if (pid.size() > 0) {
                    file.setPid(pid.get(0));
                } else {
                    file.setPid(null);
                }
                saveFileFullInfo(file);
                file.setOwner(owner);
            } else {
                file.setPid(getPidByLocal(local).get(0));
                result.add(file);
            }
            if (file.getType() == 0) {
                getAllSubFileInfo(file, result, l, userId);
            }
        }
    }

    @Override
    public int reNameDir(FileInfo fileInfo, String name, Integer owner) {
        List<Integer> pidByLocal = getPidByLocal(fileInfo.getLocation()+">"+fileInfo.getId()+"."+fileInfo.getName());
        Integer pid = null;
        if (pidByLocal.size() > 0) {
            pid = pidByLocal.get(0);
        }
        deleteFileById(fileInfo.getId(), fileInfo.getOwner(), fileInfo.getPid());
        fileInfo.setId(null);
        fileInfo.setName(name);
        Boolean saveFile = saveFile(fileInfo);
        if (!saveFile) {
            logger.error("不该出现的命名重复:fileInfo" + fileInfo + "\nname:" + name);
            return -1;
        }
        if (pid != null) {
            updateSubFilesLocation(fileInfo, pid, owner);
        }
        return fileInfo.getId();

    }

    public void updateSubFilesLocation(FileInfo fileInfo, Integer pid, Integer owner) {
        List<FileInfo> filesByFid = getFilesByFid(pid, owner);
        for (FileInfo info : filesByFid) {
            List<Integer> pidByLocal = getPidByLocal(info.getLocation()+">"+info.getId()+"."+info.getName());
            FileInfo file = new FileInfo(info.getId(),info.getPid(),info.getOwner());
            String location = fileInfo.getLocation() + ">" + fileInfo.getId() + "." + fileInfo.getName();
            file.setLocation(location);
            updateFileInfo(file);
            info.setLocation(location);
            if (pidByLocal.size() > 0) {
                updateSubFilesLocation(info, pidByLocal.get(0), owner);
            }
        }
    }

    @Override
    public String nameWithCount(String name, int count) {
        String s = name.substring(0, name.lastIndexOf("."));
        String suffix = name.substring(name.lastIndexOf(".") + 1);
        return s+"("+count+")"+"."+suffix;
    }

    @Override
    public void copyFileUsingFileChannels(File source, File dest) throws IOException {
        try (FileChannel inputChannel = new FileInputStream(source).getChannel(); FileChannel outputChannel = new FileOutputStream(dest).getChannel()) {
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        }
    }

    @Override
    public String getNewName(List<FileInfo> byFid, FileInfo f) {
        int count = 1;
        String newName;
        while (true) {
            boolean dup = false;
            newName = f.getName() + "(" + count + ")";
            if (f.getType() > 0 && f.getName().contains(".")) {
                newName = nameWithCount(f.getName(), count);
            }
            for (FileInfo fileInfo : byFid) {
                if (fileInfo.getName().equals(newName)) {
                    dup = true;
                    break;
                }
            }
            if (!dup) {
                return newName;
            }
            count++;
        }
    }

    public void coverDupFile(List<Integer> pidByLocal, Integer userId, FileInfo fileInfo) {
        List<FileInfo> filesByFid = getFilesByFid(pidByLocal.get(0), userId);
        for (FileInfo f:filesByFid) {
            if (f.getName().equals(fileInfo.getName())) {   //覆盖，即删除f及f以下的子文件（夹）
                List<FileInfo> result = new ArrayList<>();
                getAllSubFiles(f,result);     //将待移动的文件（夹）及其子目录下所有的文件（夹）保存起来
                moveToTrash(f,result);
                break;
            }
        }
    }
}
