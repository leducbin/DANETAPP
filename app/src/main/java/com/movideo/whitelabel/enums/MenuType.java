package com.movideo.whitelabel.enums;

/**
 * Holds menu types.
 */
public enum MenuType {

    /**
     * Normal menu item. Contains a title only.
     */
    NORMAL(0),

    /**
     * Special menu item. Contains a title and a icon.
     */
    SPECIAL(1);

    private final static int COUNT = 2;

    private final int id;

    MenuType(int id) {
        this.id = id;
    }

    /**
     * Returns number of types available.
     *
     * @return count.
     */
    public static int getTypeCount() {
        return COUNT;
    }

    /**
     * Returns type id.
     *
     * @return id.
     */
    public int getId() {
        return id;
    }
}
