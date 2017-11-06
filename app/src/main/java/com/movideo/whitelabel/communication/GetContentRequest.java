package com.movideo.whitelabel.communication;

import android.os.AsyncTask;
import android.util.Log;

import com.movideo.whitelabel.ContentHandler;

/**
 * Extend and customise to send different http request as a {@link AsyncTask <Params,  Progress , Result>}.
 */
public abstract class GetContentRequest<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    protected static final String LOG_TAG = "Content Request";

    private Exception exception;
    private ContentRequestListener<Result> listener;

    public GetContentRequest(ContentRequestListener<Result> listener) {
        this.listener = listener;
    }

    @Override
    protected final Result doInBackground(Params... params) {

        long startTime = System.currentTimeMillis();
        Log.d(LOG_TAG, this.getClass().getSimpleName() + " Starts");

        ContentHandler contentHandler = ContentHandler.getInstance();

        try {
            return run(contentHandler, params);
        } catch (Exception e) {
            exception = e;
            return null;
        } finally {
            long endTime = System.currentTimeMillis();
            Log.d(LOG_TAG, this.getClass().getSimpleName() + " Ends (ms) : " + (endTime - startTime));
        }
    }

    @Override
    protected void onPostExecute(Result result) {
        if (result != null) {
            listener.onRequestCompleted(result);
        } else {
            if (exception != null) {
                listener.onRequestFail(exception);
            } else {
                listener.onRequestFail(new Exception("Result return null"));
            }
        }
    }

    abstract Result run(ContentHandler contentHandler, Params... params) throws Exception;
}
