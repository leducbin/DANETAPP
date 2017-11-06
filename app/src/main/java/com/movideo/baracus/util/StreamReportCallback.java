package com.movideo.baracus.util;

import com.movideo.baracus.model.media.StreamReport;
import com.movideo.whitelabel.widgetutils.WidgetsUtils;

public class StreamReportCallback implements ICallback<StreamReport>
{
    @Override
    public void onSuccess(StreamReport result) {
        WidgetsUtils.Log.i("Stream Report", "Success: " + result.toString());
    }

    @Override
    public void onFailure(Throwable t) {
        WidgetsUtils.Log.i("Stream Report Error: " , t.getMessage());
        t.printStackTrace();
    }
}