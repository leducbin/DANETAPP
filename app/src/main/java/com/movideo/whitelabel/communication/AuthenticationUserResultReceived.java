package com.movideo.whitelabel.communication;

import com.movideo.baracus.model.user.User;

public interface AuthenticationUserResultReceived {

    public void onResult(String error, User result);
}
