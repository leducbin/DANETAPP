package com.movideo.whitelabel.communication;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.movideo.baracus.clientimpl.UserAuthenticationRequest;
import com.movideo.baracus.model.user.User;
import com.movideo.whitelabel.ContentHandler;
import com.movideo.whitelabel.R;
import com.movideo.whitelabel.WhiteLabelApplication;
import com.movideo.whitelabel.util.sharedPreference.PreferenceHelper;
import com.movideo.whitelabel.view.ProgressView;

import java.lang.ref.WeakReference;

public class AuthenticationParser extends AsyncTask<String, String, User> {

    private final WeakReference<Activity> activityWeakRef;
    private AuthenticationUserResultReceived mListener;
    private String error = null;
    private ProgressView progressView;
    private Context context;


    public AuthenticationParser(Activity activity, AuthenticationUserResultReceived listener) {
        activityWeakRef = new WeakReference<Activity>(activity);
        this.context = activity;
        this.mListener = listener;
        if (activityWeakRef.get() != null && !activityWeakRef.get().isFinishing())
            progressView = new ProgressView(activity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (activityWeakRef.get() != null && !activityWeakRef.get().isFinishing())
            progressView.show();
    }

    @Override
    protected User doInBackground(String... params) {
        UserAuthenticationRequest userAuthenticationRequest = new UserAuthenticationRequest(params[0], params[1], params[2]);
        ContentHandler contentHandler = ContentHandler.getInstance();
        User user = null;
        try {
            user = contentHandler.authenticateUser(userAuthenticationRequest);
        } catch (Exception e) {
            error = e.getMessage();
        }
        return user;
    }

    @Override
    protected void onPostExecute(User result) {
        super.onPostExecute(result);
        if (activityWeakRef.get() != null && !activityWeakRef.get().isFinishing())
            if (progressView!=null)
                progressView.dismiss();
        if (mListener != null) {
            if (error == null && result != null) {
                User user = result;
                WhiteLabelApplication whiteLabelApplication = WhiteLabelApplication.getInstance();
                whiteLabelApplication.setUser(user);
                PreferenceHelper.setDataInSharedPreference((Activity) context, context.getResources().getString(R.string.user_info), user);

                if (result.getAccessToken() != null) {
                    whiteLabelApplication.setAccessToken(user.getAccessToken());
                }
            }
            mListener.onResult(error, result);
        }
    }
}
