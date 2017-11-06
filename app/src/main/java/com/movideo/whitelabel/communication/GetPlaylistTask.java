package com.movideo.whitelabel.communication;

/**
 * Created by ThanhTam on 12/5/2016.
 */

import com.movideo.baracus.model.playlist.Playlist;
import com.movideo.whitelabel.ContentHandler;

/**
 * {@link android.os.AsyncTask} to get {@link Playlist} relate to the given collection Id.
 * Provide the play list item id as a execution method parameter.
 */
public class GetPlaylistTask extends GetContentRequest<String, Object, Playlist> {

    /**
     * Constructor with listener.
     *
     * @param listener {@link ContentRequestListener}
     */
    public GetPlaylistTask(ContentRequestListener listener) {
        super(listener);
    }

    @Override
    Playlist run(ContentHandler contentHandler, String... params) throws Exception {
        return contentHandler.getPlaylistById(params[0]);
    }
}