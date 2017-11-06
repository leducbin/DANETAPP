package com.movideo.baracus.model.user;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ThanhTam on 1/11/2017.
 */

public class Order {
    private Integer id;
    private String description;
    @SerializedName("order_date")
    private Date orderDate;
    private String amount;
    private String currency;
    @SerializedName("payment_type")
    private String paymentType;

    public Order(Integer id, String description, Date orderDate, String amount, String currency, String paymentType) {
        this.id = id;
        this.description = description;
        this.orderDate = orderDate;
        this.amount = amount;
        this.currency = currency;
        this.paymentType = paymentType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public String getAmount() {
        if(Float.parseFloat(amount) < 10000)
        {
            amount = (!TextUtils.isEmpty(amount)) ? amount : "0";
            amount = amount.trim();

            int centsIndex = amount.lastIndexOf(".");
            if(centsIndex != -1){
                while(centsIndex< amount.length() && amount.charAt(centsIndex)!= '0'){
                    centsIndex ++;
                }
                if(amount.charAt(centsIndex-1)!= '.')
                    amount = amount.substring(0, centsIndex);
                else
                    amount = amount.substring(0, centsIndex-1);
            }

            amount = String.format("%s điểm", amount);
        }
        else
        {
            NumberFormat format = new DecimalFormat("#,##0.00");
            format.setCurrency(Currency.getInstance(Locale.getDefault()));
            amount = (!TextUtils.isEmpty(amount)) ? amount : "0";
            amount = amount.trim();
            amount = format.format(Double.parseDouble(amount));
            amount = amount.replaceAll(",", "\\.");

            if (amount.endsWith(".00")) {
                int centsIndex = amount.lastIndexOf(".00");
                if (centsIndex != -1) {
                    amount = amount.substring(0, centsIndex);
                }
            }
            amount = String.format("%s đ", amount);
        }
        return amount;

    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", orderDate=" + orderDate +
                ", amount='" + amount + '\'' +
                ", currency='" + currency + '\'' +
                ", paymentType='" + paymentType + '\'' +
                '}';
    }
}
