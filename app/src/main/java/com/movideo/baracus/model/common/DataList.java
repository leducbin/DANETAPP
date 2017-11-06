package com.movideo.baracus.model.common;


/**
 * Created by rranawaka on 5/01/2016.
 */
public class DataList<T> {
    private String object;
    private int count;

    private java.util.List<T> data;

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public java.util.List<T> getData() {
        return data;
    }

    public void setData(java.util.List<T> data) {
        this.data = data;
    }
}
