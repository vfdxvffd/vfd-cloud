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

    public String formatExpire() {
        long theTime = this.expire;// 需要转换的时间秒
        int day_time = 24 * 60 * 60;
        long day = theTime / day_time;
        if (day > 0) {
            return (day+1) + "天";
        } else {
            int hour_time = 3600;
            int hour = (int) (theTime / hour_time);
            if (hour > 0) {
                return (hour+1) + "小时";
            } else {
                int min_time = 60;
                int min = (int) (theTime/min_time);
                if (min > 0) {
                    return (min+1) + "分钟";
                } else {
                    return theTime + "秒钟";
                }
            }
        }
    }
}