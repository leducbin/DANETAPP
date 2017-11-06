package com.movideo.whitelabel;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.movideo.baracus.model.VOD.Credit;
import com.movideo.whitelabel.communication.ContentRequestListener;
import com.movideo.whitelabel.communication.GetCreditTask;
import com.movideo.whitelabel.util.NetworkAvailability;
import com.movideo.whitelabel.util.Utils;
import com.movideo.whitelabel.util.ViewUtils;
import com.movideo.whitelabel.view.AddMessageDialogView;
import com.movideo.whitelabel.view.ProgressView;
import com.movideo.whitelabel.widgetutils.WidgetsUtils;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

import static com.movideo.whitelabel.ContentHandler.API_URL;
import static java.lang.Integer.parseInt;

/**
 * Created by ThanhTam on 1/6/2017.
 */

public class TVODActivity  extends AppCompatActivity implements ContentRequestListener<List<Credit>> {
    private static final String TAG = TVODActivity.class.getSimpleName();
    private String accessToken ;
    private String guid;
    private LinearLayout listButtonlayout;
    private List<Credit> credits = new ArrayList<>();
    private AddMessageDialogView dialogView;
    private ProgressView progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tvod);
        progressView = new ProgressView(this);
        progressView.show();
        listButtonlayout = (LinearLayout) findViewById(R.id.ListButtonTVOD);
        listButtonlayout.setOrientation(LinearLayout.VERTICAL);
        accessToken = WhiteLabelApplication.getInstance().getAccessToken();
        ButterKnife.bind(this);
        loadCredit();

    }

    private void loadCredit(){
        GetCreditTask getCreditTask = new GetCreditTask(this);
        WidgetsUtils.Log.d(TAG,"start load credits");
        Utils.executeInMultiThread(getCreditTask,accessToken);
    }

    @Override
    public void onRequestCompleted(List<Credit> credits) {
        if (progressView!=null)
            progressView.dismiss();
        for (final Credit credit: credits) {
            LinearLayout row = new LinearLayout(TVODActivity.this);
            row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            Button btnCredit = new Button(TVODActivity.this,null,R.style.Light);
            btnCredit = ViewUtils.setButtonDynamicStyle(btnCredit,TVODActivity.this);
            btnCredit.setId(parseInt(credit.getId()));
            btnCredit.setAllCaps(false);
            btnCredit.setText(Html.fromHtml("<b><big>" + credit.getTitle() + "</big></b>" +  "<br />" +
                    "<small style='font-size:x-small;'>" + credit.getPrice() + "</small>"));
            btnCredit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendRestRequest(credit.getId());
                }
            });
            row.addView(btnCredit);
            listButtonlayout.addView(row);
        }
    }

    @Override
    public void onRequestFail(Throwable throwable) {
        if (progressView!=null)
            progressView.dismiss();
        Toast.makeText(TVODActivity.this,"Fail to get credits",Toast.LENGTH_LONG).show();
        Log.e(TAG, throwable.getMessage(), throwable);
    }

    private void sendRestRequest(String id) {
        GetRestResponseTask getRestResponseTask = new GetRestResponseTask();
        Utils.executeInMultiThread(getRestResponseTask,id);
    }

    private class GetRestResponseTask extends AsyncTask<String, Void,String > {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressView.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                //http://api.danet.vn/credits/21584/purchase
                URL url = new URL(API_URL + "credits/" + params[0]+ "/purchase");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                try {
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Movideo-Auth",accessToken);
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    String body = "{\"return_url\":\"http://www.danet.vn/account\"}";
                    OutputStream output = new BufferedOutputStream(conn.getOutputStream());
                    output.write(body.getBytes("UTF-8"));
                    output.close();
                    conn.connect();
                    //Log.d(TAG,conn.getHeaderField("movideo-auth"));
                    Log.d(TAG, body);
                    Log.d(TAG,conn.getResponseCode()+" "+conn.getResponseMessage());
                    StringBuilder sb = new StringBuilder();
                    String line;
                    BufferedReader bufferedReader;
                    if( (conn.getResponseCode()>=200) && (conn.getResponseCode() < 300) ) {
                        bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    }
                    else{
                        bufferedReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    }
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    bufferedReader.close();
                    Log.d("API purchase", sb.toString());
                    //return received string
                    return sb.toString();

                } finally {
                    conn.disconnect();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String inputStream) {
            super.onPostExecute(inputStream);
            if (progressView!=null)
                progressView.dismiss();
            StringBuilder sb = new StringBuilder();
            try {
                JSONObject mainObject = new JSONObject(inputStream);
                if(mainObject.has("session")) {
                    //sb.append(mainObject.getString("session"));
                    navigateToPaymentWebView(mainObject.getString("session"));
                }
                else {
                    JSONObject error = mainObject.getJSONObject("error");
                    String error_message = error.getString("message");
                    sb.append(error_message);
                    dialogView = new AddMessageDialogView(TVODActivity.this,"Co lỗi xảy ra",sb.toString()+". Xin vui lòng quay lại sau.","Ok");
                    dialogView.show();
                }

            }
            catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    private void navigateToPaymentWebView(String guid){
        if (NetworkAvailability.chkStatus(this)) {
            Intent intent = new Intent(this, ExtraActivity.class);
            String url = "https://payment.danet.vn/?guid="+ guid +"&accessToken=" + accessToken;
            Log.d(TAG,url);
            intent.putExtra("url", "https://payment.danet.vn/?guid="+ guid +"&accessToken=" + accessToken);
            startActivity(intent);
        } else {
            Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
        }
    }
}
