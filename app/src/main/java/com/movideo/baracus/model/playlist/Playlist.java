package com.movideo.baracus.model.playlist;

import com.google.gson.annotations.SerializedName;
import com.movideo.baracus.model.common.Image;
import com.movideo.baracus.model.product.Product;

import java.io.Serializable;
import java.util.ArrayList;
//import java.util.Date;
import java.util.Date;
import java.util.List;

/**
 * Created by ThanhTam on 12/2/2016.
 */

public class Playlist implements Serializable {
    private static final long serialVersionUID = 1L;
    @SerializedName("playlist_name")
    private String name;
    private String type;
    @SerializedName("id")
    private Integer id;
    @SerializedName("total_count")
    private Integer count;
    private Integer extend;
    private String extend_name;
    @SerializedName("list")
    private List<Product> list_product = new ArrayList<Product>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<Product> getProductList() {
        return list_product;
    }

    public void setProductList(List<Product> list_product) {
        this.list_product = list_product;
    }

    public String getExtend_name() {
        return extend_name;
    }

    public void setExtend_name(String extend_name) {
        this.extend_name = extend_name;
    }

    public Integer getExtend() {
        return extend;
    }

    public void setExtend(Integer extend) {
        this.extend = extend;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Data[")
                .append("id : ").append(id).append(".\n")
                .append("name : ").append(name).append(",\n")
                .append("type : ").append(type).append(",\n")
                .append("count : ").append(count).append(",\n")
                .append("extend : ").append(extend).append(",\n")
                .append("extend_name : ").append(extend_name).append(",\n");
                //.append("]").append("Data : [,\n").append(list_product + "\n  ]");
        return sb.toString();

    }


}
