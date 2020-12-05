package com.vfd.demo.mapper;

import com.vfd.demo.bean.UserAccInfo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @PackageName: com.vfd.cloud.dao
 * @InterfaceName: UserLoginDao
 * @Description: 负责用户的登陆注册操作
 * @author: vfdxvffd
 * @date: 11/26/20 9:47 PM
 */
@Mapper
@Repository
public interface UserLoginMapper {

    /**
     * 根据用户email查询用户id
     * @param email
     * @return
     */
    Integer getUserIdByEmail(String email);

    /**
     * 用户登陆，根据用户的邮箱密码获取用户id
     * @param email
     * @param password
     * @return 0表示密码错误，正数则为用户id
     */
    Integer getUserId(String email, String password);

    /**
     * 用户注册
     * @param email
     * @param password
     * @return
     */
    Boolean addUser(String email, String password);

    /**
     * 修改密码
     * @param email
     * @param password
     * @return
     */
    Boolean updateUserPassword(String email, String password);

    /**
     * 根据用户email获取到用户的账号信息
     * @param email
     * @return
     */
    UserAccInfo getUserInfoByEmail(String email);
}