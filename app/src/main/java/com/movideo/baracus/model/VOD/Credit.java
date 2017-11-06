package com.movideo.baracus.model.VOD;

import android.text.TextUtils;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Created by ThanhTam on 1/6/2017.
 */

public class Credit implements Serializable {
    private String currency;
    private String price;
    private String id;
    private String title;
    private String description;

    public Credit(String currency, String price, String id, String title, String description) {
        this.currency = currency;
        this.price = price;
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPrice() {
        NumberFormat format = new DecimalFormat("#,##0.00");
        format.setCurrency(Currency.getInstance(Locale.getDefault()));
        price = (!TextUtils.isEmpty(price)) ? price : "0";
        price = price.trim();
        price = format.format(Double.parseDouble(price));
        price = price.replaceAll(",", "\\.");

        if (price.endsWith(".00")) {
            int centsIndex = price.lastIndexOf(".00");
            if (centsIndex != -1) {
                price = price.substring(0, centsIndex);
            }
        }
        price = String.format("%s đ", price);
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("{")
                .append("currency : ").append(currency).append(",\n")
                .append("price : ").append(price).append(",\n")
                .append("id : ").append(id).append(",\n")
                .append("title : ").append(title).append(",\n")
                .append("description : ").append(description).append("\n")
                .append("}\n");
        return sb.toString();
    }
}
