package com.vfd.demo.service;

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
     * @param password
     * @return -1表示用户不存在，0表示密码错误，正数表示用户id
     */
    Integer login(String email, String password);

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
}
