package com.movideo.baracus.model.user;

import java.io.Serializable;

public class ProductPurchase implements Serializable {
    private String offering_id;
    private String payment_type;

    public ProductPurchase(String offeringId, String payment_type) {
        this.offering_id = offeringId;
        this.payment_type = payment_type;
    }

    public String getOffering_id() {
        return offering_id;
    }

    public void setOffering_id(String offering_id) {
        this.offering_id = offering_id;
    }

    public String getPayment_type() {
        return payment_type;
    }

    public void setPayment_type(String payment_type) {
        this.payment_type = payment_type;
    }
}
