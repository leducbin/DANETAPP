package com.movideo.whitelabel.communication;

/**
 * Created by ThanhTam on 12/5/2016.
 */

import com.movideo.baracus.model.VOD.Credit;
import com.movideo.baracus.model.user.Order;
import com.movideo.whitelabel.ContentHandler;

import java.util.List;

/**
 * {@link android.os.AsyncTask} to get {@link List<Order>} relate to the given collection Id.
 * Provide the play list item id as a execution method parameter.
 */
public class GetOrderTask extends GetContentRequest<String, Object, List<Order>> {

    /**
     * Constructor with listener.
     *
     * @param listener {@link ContentRequestListener}
     */
    public GetOrderTask(ContentRequestListener listener) {
        super(listener);
    }

    @Override
    List<Order> run(ContentHandler contentHandler, String... params) throws Exception {
        return contentHandler.getAllOrders(params[0]);
    }
}