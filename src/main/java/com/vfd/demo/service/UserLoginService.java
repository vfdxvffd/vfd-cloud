package com.vfd.demo.service;

import com.vfd.demo.bean.UserAccInfo;

/**
 * @PackageName: com.vfd.demo.service
 * @InterfaceName: UserLoginService
 * @Description:
 * @author: vfdxvffd
 * @date: 12/5/20 7:09 PM
 */
public interface UserLoginService {

    /**
     * 用户登陆的检查
     * @param email
     * @return 根据用户登陆邮箱返回用户信息
     */
    UserAccInfo login(String email);

    /**
     * 用户注册
     * @param name
     * @param email
     * @param password
     * @return -1表示用户已存在，0表示失败，id表示新加入的用户id，可以存作缓存
     */
    Integer register(String name, String email, String password);

    /**
     * 判断此邮箱是否是在注册的用户
     * @param email
     * @return
     */
    Boolean isExist(String email);

    /**
     * 修改密码
     * @param email
     * @param password
     * @return
     */
    Boolean updateUserPassword (String email, String password);

    /**
     * 根据用户的id获得名字
     * @param id
     * @return
     */
    String getNameById (Integer id);
}
