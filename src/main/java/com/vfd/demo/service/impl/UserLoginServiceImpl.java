package com.vfd.demo.service.impl;

import com.vfd.demo.mapper.UserLoginMapper;
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
public class UserLoginService {

    @Autowired
    UserLoginMapper userLoginMapper;

    /**
     * 用户登陆的检查
     * @param email
     * @param password
     * @return -1表示用户不存在，0表示密码错误，正数表示用户id
     */
    public Integer login(String email, String password) {
        if (userLoginMapper.getUserIdByEmail(email) == null) {
            return -1;
        } else {
            Integer id = userLoginMapper.getUserId(email,password);
            return id == null? 0:id;
        }
    }

    /**
     * 用户注册
     * @param name
     * @param email
     * @param password
     * @return -1表示用户已存在，0表示失败，id表示新加入的用户id，可以存作缓存
     */
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

    /**
     * 判断此邮箱是否是在注册的用户
     * @param email
     * @return
     */
    public Boolean isExist(String email) {
        Integer id = userLoginMapper.getUserIdByEmail(email);
        return id==null? false:true;
    }

    /**
     * 修改密码
     * @param email
     * @param password
     * @return
     */
    public Boolean updateUserPassword (String email, String password) {
        return userLoginMapper.updateUserPassword(email,password);
    }
}