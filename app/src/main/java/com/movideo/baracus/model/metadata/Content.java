package com.movideo.baracus.model.metadata;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by rranawaka on 14/12/2015.
 */
public class Content implements Serializable
{

    public enum Type {
        playlist,
        collection,
        carousel,
        content,
        navigation,
        genre,
        search,
        image,
        button,
        text,
        subnavigation,
        filter
    }


    private List<Content> items;
    private Background background;
    private String line1;
    private String line2;
    private String alignment;
    private String href;
    private String theme;
    private String offering;
    private String category;
    private String title;
    private String genre;
    private Type type;
    private String name;
    private String identifier;
    private String collection_id;
    @SerializedName("id")
    private String playlist_id;
    private int limit;
    private Content content;

    public Content() {
    }

    public Content(Type type, String name, String identifier, String playlist_id, int limit, List<Content> items) {
        this.type = type;
        this.name = name;
        this.identifier = identifier;
        this.playlist_id = playlist_id;
        this.limit = limit;
        this.items = items;
    }

    public String getCollection_id() {
        if (null != collection_id)
            return collection_id;
        return identifier;
    }

    public String getPlaylist_id() {
        if(null!=playlist_id)
            return playlist_id;
        return identifier;
    }


    public void setCollection_id(String collection_id) {
        this.collection_id = collection_id;
    }

    public void setPlaylist_id(String playlist_id) {
        this.playlist_id = playlist_id;
    }


    public Background getBackground() {
        return background;
    }

    public void setBackground(Background background) {
        this.background = background;
    }

    public String getLine1() {
        return line1;
    }

    public void setLine1(String line1) {
        this.line1 = line1;
    }

    public String getLine2() {
        return line2;
    }

    public void setLine2(String line2) {
        this.line2 = line2;
    }

    public String getAlignment() {
        return alignment;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getOffering() {
        return offering;
    }

    public void setOffering(String offering) {
        this.offering = offering;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }



    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentifier() {
        if (null != identifier)
            return identifier;
        return playlist_id;
    }

    public void setIdentifier(String identifier) {

        this.identifier = identifier;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public List<Content> getItems() {
        return items;
    }

    public void setItems(List<Content> items) {
        this.items = items;
    }
    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

}
