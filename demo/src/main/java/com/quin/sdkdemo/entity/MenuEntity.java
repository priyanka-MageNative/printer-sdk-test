package com.quin.sdkdemo.entity;

public class MenuEntity {
    public static final int TEXT=0X01;
    public static final int BARCODE=0X02;
    public static final int QR_CODE=0X03;
    public static final int LINE=0X04;
    public static final int LINE_FRAME=0X05;
    public static final int PICTURE=0X06;
    public static final int SAMPLE=0X07;


    private String name;
    private int id ;

    public MenuEntity(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
