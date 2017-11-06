package com.movideo.whitelabel.communication;

/**
 * Created by ThanhTam on 12/5/2016.
 */

import com.movideo.baracus.model.VOD.Credit;
import com.movideo.whitelabel.ContentHandler;

import java.util.List;

/**
 * {@link android.os.AsyncTask} to get {@link List<Credit>} relate to the given collection Id.
 * Provide the play list item id as a execution method parameter.
 */
public class GetCreditTask extends GetContentRequest<String, Object, List<Credit>> {

    /**
     * Constructor with listener.
     *
     * @param listener {@link ContentRequestListener}
     */
    public GetCreditTask(ContentRequestListener listener) {
        super(listener);
    }

    @Override
    List<Credit> run(ContentHandler contentHandler, String... params) throws Exception {
        return contentHandler.getAllCredits(params[0]);
    }
}