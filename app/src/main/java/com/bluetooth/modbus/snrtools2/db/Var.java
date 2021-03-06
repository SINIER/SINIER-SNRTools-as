package com.bluetooth.modbus.snrtools2.db;

import org.greenrobot.greendao.annotation.*;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END

/**
 * 实时变量配置
 */
@Entity(nameInDb = "Var")
public class Var implements java.io.Serializable {

    @Id(autoincrement = true)
    private Long id;

    @Property(nameInDb = "name")
    private String name;

    @Property(nameInDb = "nameHexNo")
    private String nameHexNo;

    @Property(nameInDb = "hexNo")
    private String hexNo;

    @Property(nameInDb = "type")
    private String type;

    @Property(nameInDb = "count")
    private String count;

    @Property(nameInDb = "unit")
    private String unit;

    @Property(nameInDb = "btAddress")
    private String btAddress;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    @Generated
    public Var() {
    }

    public Var(Long id) {
        this.id = id;
    }

    @Generated
    public Var(Long id, String name, String nameHexNo, String hexNo, String type, String count, String unit, String btAddress) {
        this.id = id;
        this.name = name;
        this.nameHexNo = nameHexNo;
        this.hexNo = hexNo;
        this.type = type;
        this.count = count;
        this.unit = unit;
        this.btAddress = btAddress;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 变量名称
     */
    public String getName() {
        return name;
    }

    /**
     * 变量名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 变量名称hexNo
     */
    public String getNameHexNo() {
        return nameHexNo;
    }

    /**
     * 变量名称hexNo
     */
    public void setNameHexNo(String nameHexNo) {
        this.nameHexNo = nameHexNo;
    }

    /**
     * 十六进制编号（例：0000）
     */
    public String getHexNo() {
        return hexNo;
    }

    /**
     * 十六进制编号（例：0000）
     */
    public void setHexNo(String hexNo) {
        this.hexNo = hexNo;
    }

    /**
     * 变量类型
     */
    public String getType() {
        return type;
    }

    /**
     * 变量类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 变量选项数量/小数位
     */
    public String getCount() {
        return count;
    }

    /**
     * 变量选项数量/小数位
     */
    public void setCount(String count) {
        this.count = count;
    }

    /**
     * 变量单位/选项字符串指针
     */
    public String getUnit() {
        return unit;
    }

    /**
     * 变量单位/选项字符串指针
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * 连接设备地址
     */
    public String getBtAddress() {
        return btAddress;
    }

    /**
     * 连接设备地址
     */
    public void setBtAddress(String btAddress) {
        this.btAddress = btAddress;
    }

    // KEEP METHODS - put your custom methods here

    @Override
    public String toString() {
        return "===========hexNo==="+hexNo+",type==="+type+",count==="+count+",unit==="+unit+",name==="+name+",nameHexNo==="+nameHexNo;
    }
    // KEEP METHODS END

}
