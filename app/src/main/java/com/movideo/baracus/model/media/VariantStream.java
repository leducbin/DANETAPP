package com.movideo.baracus.model.media;

import java.io.Serializable;

/**
 * Created by lamtuong on 6/10/16.
 */
public class VariantStream implements Serializable {
    private String src;
    private SecureStream drm;
    private String type;
    private int heartbeat;
    private long progress;
    private String stream_code;

    public VariantStream() {

    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public SecureStream getDrm() {
        return drm;
    }

    public void setDrm(SecureStream drm) {
        this.drm = drm;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public String getStream_code() {
        return stream_code;
    }

    public void setStream_code(String stream_code) {
        this.stream_code = stream_code;
    }
}
