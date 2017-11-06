package com.movideo.whitelabel.model;

import com.movideo.baracus.model.metadata.Content;
import com.movideo.whitelabel.enums.MenuType;

/**
 * Holds a menu item information
 */
public class MenuItem {

    private String id;
    private String title;
    private int iconResId;
    private MenuType menuType;

    private Content content;

    /**
     * No argument constructor.
     */
    public MenuItem() {
    }

    /**
     * Constructor with arguments.
     *
     * @param id        Item id.
     * @param title     Item title.
     * @param iconResId Icon resource id.
     * @param menuType  {@link MenuType}.
     */
    public MenuItem(String id, String title, int iconResId, MenuType menuType) {
        this.id = id;
        this.title = title;
        this.iconResId = iconResId;
        this.menuType = menuType;
    }

    public MenuItem(String id, String title, int iconResId, MenuType menuType, Content content) {
        this.id = id;
        this.title = title;
        this.iconResId = iconResId;
        this.menuType = menuType;
        this.content = content;
    }

    /**
     * Returns item id.
     *
     * @return Item id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets item id.
     *
     * @param id Item id.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns item title.
     *
     * @return Item title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets item title.
     *
     * @param title Item title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns icon resource id.
     *
     * @return Icon resource id.
     */
    public int getIconResId() {
        return iconResId;
    }

    /**
     * Sets icon resource id.
     *
     * @param iconResId Icon resource id.
     */
    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    /**
     * Returns menu type
     *
     * @return {@link MenuType}.
     */
    public MenuType getMenuType() {
        return menuType;
    }

    /**
     * Sets menu type.
     *
     * @param menuType {@link MenuType}.
     */
    public void setMenuType(MenuType menuType) {
        this.menuType = menuType;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }
}
