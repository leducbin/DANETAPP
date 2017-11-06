package com.movideo.whitelabel.communication;

import com.movideo.baracus.model.user.User;
import com.movideo.whitelabel.ContentHandler;
import com.movideo.whitelabel.WhiteLabelApplication;

/**
 * {@link android.os.AsyncTask} to update {@link User}.
 */
public class UpdateUserDetailsTask  extends GetContentRequest<User, Object, User> {

    public UpdateUserDetailsTask(ContentRequestListener<User> listener) {
        super(listener);
    }

    @Override
    User run(ContentHandler contentHandler, User... params) throws Exception {
        return contentHandler.updateUser(WhiteLabelApplication.getInstance().getAccessToken(), params[0]);
    }
}
