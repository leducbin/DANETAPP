package com.movideo.baracus.model.media;

import java.io.Serializable;

public class StreamReport implements Serializable {
    private String time_watched;
    private String stream_code;

    public StreamReport(String time_watched, String stream_code) {
        this.time_watched = time_watched;
        this.stream_code = stream_code;
    }

    public String getTime_watched() {
        return time_watched;
    }

    public void setTime_watched(String time_watched) {
        this.time_watched = time_watched;
    }

    public String getStream_code() {
        return stream_code;
    }

    public void setStream_code(String stream_code) {
        this.stream_code = stream_code;
    }

    @Override
    public String toString() {
        return "StreamReport{" +
                "time_watched='" + time_watched + '\'' +
                ", stream_code='" + stream_code + '\'' +
                '}';
    }
}
