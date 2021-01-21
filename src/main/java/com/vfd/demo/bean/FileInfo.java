package com.vfd.demo.bean;

/**
 * @PackageName: com.vfd.demo.bean
 * @ClassName: FileInfo
 * @Description: 文件的基本信息
 * @author: vfdxvffd
 * @date: 2021/1/21 下午7:57
 */
public class FileInfo {

    private Integer id;         //文件id
    private String name;        //文件名字
    private Integer len;        //文件长度
    private Integer pid;        //文件父目录id
    private String location;    //文件路径
    private Integer type;       //文件类型

    public FileInfo() {
    }

    public FileInfo(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public FileInfo(String name, Integer len, Integer pid, String location, Integer type) {
        this.name = name;
        this.len = len;
        this.pid = pid;
        this.location = location;
        this.type = type;
    }

    public FileInfo(Integer id, String name, Integer len, Integer pid, String location, Integer type) {
        this.id = id;
        this.name = name;
        this.len = len;
        this.pid = pid;
        this.location = location;
        this.type = type;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", len=" + len +
                ", pid=" + pid +
                ", location='" + location + '\'' +
                ", type=" + type +
                '}';
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

    public Integer getLen() {
        return len;
    }

    public void setLen(Integer len) {
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
}