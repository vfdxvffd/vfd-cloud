package com.vfd.demo.bean;

/**
 * @PackageName: com.vfd.demo.bean
 * @ClassName: UserInfo
 * @Description:
 * @author: vfdxvffd
 * @date: 12/5/20 8:49 PM
 */
public class UserAccInfo {

    private Integer id;
    private String email;
    private String password;
    private String name;

    public UserAccInfo(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}