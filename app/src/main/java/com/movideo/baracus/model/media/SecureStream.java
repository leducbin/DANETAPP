package com.movideo.baracus.model.media;

import java.io.Serializable;

/**
 * Created by lamtuong on 6/10/16.
 */
public class SecureStream implements Serializable {

    public SecureStream() {

    }

    public String getBoot_url() {
        return boot_url;
    }

    public void setBoot_url(String boot_url) {
        this.boot_url = boot_url;
    }

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }

    String boot_url;
    String company_name;
}
