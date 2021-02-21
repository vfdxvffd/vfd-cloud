package com.vfd.demo.bean;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @PackageName: com.vfd.demo.bean
 * @ClassName: TrashInfo
 * @Description:
 * @author: vfdxvffd
 * @date: 2021/2/19 上午7:15
 */
public class TrashInfo {

    private Integer id;         //文件id
    private String name;        //文件名字
    private Long len;        //文件长度
    private Integer pid;        //文件父目录id
    private String location;    //文件路径
    private Integer type;       //文件类型
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp time;          //文件上传时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp delete;          //文件删除时间
    private Integer owner;          //拥有者的id
    private Long expire;

    public TrashInfo() {
    }

    public TrashInfo(FileInfo fileInfo, Timestamp delete, Long expire) {
        this.id = fileInfo.getId();
        this.name = fileInfo.getName();
        this.len = fileInfo.getLen();
        this.pid = fileInfo.getPid();
        this.location = fileInfo.getLocation();
        this.type = fileInfo.getType();
        this.time = fileInfo.getTime();
        this.delete = delete;
        this.owner = fileInfo.getOwner();
        this.expire = expire;
    }

    public TrashInfo(Integer id, String name, Long len, Integer pid, String location, Integer type, Timestamp time, Timestamp delete, Integer owner, Long expire) {
        this.id = id;
        this.name = name;
        this.len = len;
        this.pid = pid;
        this.location = location;
        this.type = type;
        this.time = time;
        this.delete = delete;
        this.owner = owner;
        this.expire = expire;
    }

    public String gainTime() {
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(new Date(this.delete.getTime()));
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getLen() {
        return len;
    }

    public void setLen(Long len) {
        this.len = len;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public Timestamp getDelete() {
        return delete;
    }

    public void setDelete(Timestamp delete) {
        this.delete = delete;
    }

    public Integer getOwner() {
        return owner;
    }

    public void setOwner(Integer owner) {
        this.owner = owner;
    }

    public Long getExpire() {
        return expire;
    }

    public void setExpire(Long expire) {
        this.expire = expire;
    }

    public String convertToSize() {
        if (this.type == 0)
            return "--";
        BigDecimal fileSize = new BigDecimal(this.len);
        BigDecimal param = new BigDecimal(1024);
        int count = 0;
        while(fileSize.compareTo(param) > 0 && count < 5)
        {
            fileSize = fileSize.divide(param);
            count++;
        }
        DecimalFormat df = new DecimalFormat("#.##");
        String result = df.format(fileSize) + "";
        switch (count) {
            case 0:
                result += "B";
                break;
            case 1:
                result += "KB";
                break;
            case 2:
                result += "MB";
                break;
            case 3:
                result += "GB";
                break;
            case 4:
                result += "TB";
                break;
            case 5:
                result += "PB";
                break;
        }
        return result;
    }
}