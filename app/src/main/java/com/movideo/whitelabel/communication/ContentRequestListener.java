package com.movideo.whitelabel.communication;

/**
 * Implement and pass into a http request method to listen to the http response.
 */
public interface ContentRequestListener<Result> {

    /**
     * Called on http request completed and received the result.
     *
     * @param result returns the result based on the {@link <Result>}
     */
    void onRequestCompleted(Result result);

    /**
     * Called on http request failed.
     *
     * @param throwable {@link Throwable}
     */
    void onRequestFail(Throwable throwable);
}
