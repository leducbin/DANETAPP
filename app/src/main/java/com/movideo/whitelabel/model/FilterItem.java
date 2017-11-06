package com.movideo.whitelabel.model;

public class FilterItem {

    private String title;

    public FilterItem(Boolean isSelected, String title) {
        this.isSelected = isSelected;
        this.title = title;
    }

    public Boolean getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(Boolean isSelected) {
        this.isSelected = isSelected;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private Boolean isSelected;
}
