package com.vfd.demo.bean;

/**
 * @PackageName: com.vfd.demo.bean
 * @ClassName: ShareInfo
 * @Description:
 * @author: vfdxvffd
 * @date: 2021/2/14 下午10:53
 */
public class ShareInfo {

    private String link;
    private String pass;
    private String uuid;
    private Long expire;
    private String name;
    private Integer type;

    public ShareInfo() {
    }

    public ShareInfo(String link, String pass, String uuid, Long expire, String name, Integer type) {
        this.link = link;
        this.pass = pass;
        this.uuid = uuid;
        this.expire = expire;
        this.name = name;
        this.type = type;
    }

    @Override
    public String toString() {
        return "ShareInfo{" +
                "link='" + link + '\'' +
                ", pass='" + pass + '\'' +
                ", uuid='" + uuid + '\'' +
                ", expire=" + expire +
                ", name='" + name + '\'' +
                ", type=" + type +
                '}';
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public Long getExpire() {
        return expire;
    }

    public void setExpire(Long expire) {
        this.expire = expire;
    }
}