package com.movideo.baracus.model.VOD;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Created by ThanhTam on 1/6/2017.
 */

public class Subscription implements Serializable {
    private String object;
    private String name;
    private String service_id;
    private String status;
    private Integer period;
    private String unit;
    private String auto_renew;
    private Integer id;
    private String payment;
    private String currency;
    private String price;

    public Subscription(String object, String name, String service_id, String status, Integer period, String unit, String auto_renew, Integer id, String payment, String currency, String price) {
        this.object = object;
        this.name = name;
        this.service_id = service_id;
        this.status = status;
        this.period = period;
        this.unit = unit;
        this.auto_renew = auto_renew;
        this.id = id;
        this.payment = payment;
        this.currency = currency;
        this.price = price;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getName() {
        String name;
        switch (unit) {
            case "years":
                name= (period == 1) ? "1 Năm" : period + " Năm";
                break;
            case "months":
                name = (period == 1) ? "1 Tháng" : period + " Tháng";
                break;
            case "days":
                name = (period == 1) ? "1 Ngày" : period + " Ngày";
                break;
            default:
                name="";
                break;
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getService_id() {
        return service_id;
    }

    public void setService_id(String service_id) {
        this.service_id = service_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getAuto_renew() {
        return auto_renew;
    }

    public void setAuto_renew(String auto_renew) {
        this.auto_renew = auto_renew;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPrice() {
        if(payment.equals("credits"))
        {
            price = (!TextUtils.isEmpty(price)) ? price : "0";
            price = price.trim();

            if (price.endsWith(".00")) {
                int centsIndex = price.lastIndexOf(".00");
                if (centsIndex != -1) {
                    price = price.substring(0, centsIndex);
                }
            }
            price = String.format("%s điểm", price);
        }
        else
        {
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
        }
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "object='" + object + '\'' +
                ", name='" + name + '\'' +
                ", service_id='" + service_id + '\'' +
                ", status='" + status + '\'' +
                ", period=" + period +
                ", unit='" + unit + '\'' +
                ", auto_renew='" + auto_renew + '\'' +
                ", id=" + id +
                ", payment='" + payment + '\'' +
                ", currency='" + currency + '\'' +
                ", price='" + price + '\'' +
                '}';
    }
}
