package com.vfd.demo.service.impl;

import com.vfd.demo.mapper.UserLoginMapper;
import com.vfd.demo.service.UserLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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


    public Integer login(String email, String password) {
        if (userLoginMapper.getUserIdByEmail(email) == null) {
            return -1;
        } else {
            Integer id = userLoginMapper.getUserId(email,password);
            return id == null? 0:id;
        }
    }

    public Integer register(String name, String email, String password) {
        if (userLoginMapper.getUserIdByEmail(email) != null) {
            return -1;
        }
        if (userLoginMapper.addUser(email,password)) {
            return userLoginMapper.getUserIdByEmail(email);
        } else {
            return 0;
        }
    }

    public Boolean isExist(String email) {
        Integer id = userLoginMapper.getUserIdByEmail(email);
        return id==null? false:true;
    }

    public Boolean updateUserPassword (String email, String password) {
        return userLoginMapper.updateUserPassword(email,password);
    }
}