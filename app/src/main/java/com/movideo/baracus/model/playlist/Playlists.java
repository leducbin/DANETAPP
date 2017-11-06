package com.movideo.baracus.model.playlist;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ThanhTam on 12/2/2016.
 */

public class Playlists implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Playlist> playlist = new ArrayList<>();

    public List<Playlist> getPlaylist() {
        return playlist;
    }

    public void setPlaylist(List<Playlist> playlist) {
        this.playlist = playlist;
    }
    public Playlists(List<Playlist> playlist){
        this.playlist=playlist;
    }
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Playlist[")
                .append(playlist)
                .append(",n")
                .append("]");
        return sb.toString();
    }
}
