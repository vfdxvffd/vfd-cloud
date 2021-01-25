package com.vfd.demo.service.impl;

import com.vfd.demo.bean.FileInfo;
import com.vfd.demo.bean.UserAccInfo;
import com.vfd.demo.mapper.FileOperationMapper;
import com.vfd.demo.mapper.UserLoginMapper;
import com.vfd.demo.service.UserLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @PackageName: com.vfd.cloud.service
 * @ClassName: UserLoginService
 * @Description:
 * @author: vfdxvffd
 * @date: 11/26/20 10:36 PM
 */
@Service
public class UserLoginServiceImpl implements UserLoginService {

    @Autowired
    UserLoginMapper userLoginMapper;
    @Autowired
    FileOperationMapper fileOperationMapper;


    public Integer login(String email, String password) {
        UserAccInfo accInfo = userLoginMapper.getUserInfoByEmail(email);
        if (accInfo == null) {
            return -1;
        } else {
            return accInfo.getPassword().equals(password)?accInfo.getId():0;
        }
    }

    //0表示失败，-1表示重复
    @Transactional
    public Integer register(String name, String email, String password) {
        if (userLoginMapper.getUserIdByEmail(email) != null) {
            return -1;
        }
        UserAccInfo userAccInfo = new UserAccInfo(email, password);
        Boolean addUser = userLoginMapper.addUser(userAccInfo);
        FileInfo fileInfo = new FileInfo(-1*userAccInfo.getId(),"全部文件",0L,0,"",0);
        Boolean saveFile = fileOperationMapper.mkDir(fileInfo);
        if (addUser && saveFile) {
            return userAccInfo.getId();
        } else {
            return 0;
        }
    }

    public Boolean isExist(String email) {
        Integer id = userLoginMapper.getUserIdByEmail(email);
        return id != null;
    }

    public Boolean updateUserPassword (String email, String password) {
        return userLoginMapper.updateUserPassword(email,password);
    }
}