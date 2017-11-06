package com.movideo.baracus.clientimpl;

import com.movideo.baracus.model.media.ProductStream;
import com.movideo.baracus.model.media.SecureStream;
import com.movideo.baracus.model.media.VariantStream;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ProductStreamParser {

    private static final String TAG_HD = "hd";
    private static final String TAG_SD = "sd";
    private String TAG_SMIL = "smil",
            TAG_LINK = "src",
            TAG_STREAMS = "streams",
            TAG_DRM = "drm",
            TAG_BOOT_ADDRESS = "boot_url",
            TAG_COMPANY_NAME = "company_name",
            TAG_TYPE = "type",
            TAG_HEARTBEAT = "heartbeat",
            TAG_STREAM_CODE = "id",
            TAG_PROGRESS = "progress";



    public ProductStream parse(ResponseBody response) throws JSONException, IOException {
        ProductStream productStream = new ProductStream();

        VariantStream variantStream = new VariantStream();

        String json = response.string().toString();

        JSONObject jsonObj = new JSONObject(json);
        JSONObject jsonObjStreams = jsonObj.getJSONObject(TAG_STREAMS);

        if (jsonObjStreams.has(TAG_HD))
            productStream.setHd(variantStream);
        else
            productStream.setSd(variantStream);

        JSONObject jsonObjVarian = jsonObjStreams.getJSONObject(jsonObjStreams.keys().next());

        variantStream.setSrc(jsonObjVarian.getString(TAG_LINK));

        if (jsonObjVarian.has(TAG_HEARTBEAT)) variantStream.setHeartbeat(Integer.parseInt(jsonObjVarian.getString(TAG_HEARTBEAT)));
        else variantStream.setHeartbeat(-1);

        if (jsonObjVarian.has(TAG_PROGRESS)) variantStream.setProgress(Long.parseLong(jsonObjVarian.getString(TAG_PROGRESS)));
        else variantStream.setProgress(0);

        if (jsonObjVarian.has(TAG_STREAM_CODE)) variantStream.setStream_code(jsonObjVarian.getString(TAG_STREAM_CODE));

        if (jsonObjVarian.has(TAG_TYPE)) variantStream.setType(jsonObjVarian.getString(TAG_TYPE));

        if (jsonObjVarian.has(TAG_DRM)) {
            SecureStream secureStream = new SecureStream();
            variantStream.setDrm(secureStream);

            JSONObject jsonObjDRM = jsonObjVarian.getJSONObject(TAG_DRM);
            secureStream.setBoot_url(jsonObjDRM.getString(TAG_BOOT_ADDRESS));
            secureStream.setCompany_name(jsonObjDRM.getString(TAG_COMPANY_NAME));
        }
        return productStream;
    }
}
