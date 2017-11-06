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

import com.movideo.baracus.model.VOD.Subscription;
import com.movideo.whitelabel.communication.ContentRequestListener;
import com.movideo.whitelabel.communication.GetSubscriptionTask;
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

/**
 * Created by ThanhTam on 1/6/2017.
 */

public class SVODActivity  extends AppCompatActivity implements ContentRequestListener<List<Subscription>> {
    private static final String TAG = SVODActivity.class.getSimpleName();
    private String accessToken ;
    private String type;
    private String guid;
    private LinearLayout listButtonlayout;
    private List<Subscription> subscriptions = new ArrayList<>();
    private URL url;
    private HttpURLConnection conn;
    private AddMessageDialogView dialogView;
    ProgressView progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_svod);
        progressView = new ProgressView(this);
        progressView.show();
        type = getIntent().getStringExtra("type");
        listButtonlayout = (LinearLayout) findViewById(R.id.ListButtonSVOD);
        listButtonlayout.setOrientation(LinearLayout.VERTICAL);
        accessToken = WhiteLabelApplication.getInstance().getAccessToken();
        ButterKnife.bind(this);
        loadSubscription();



    }

    private void loadSubscription(){
        GetSubscriptionTask getSubscriptionTask = new GetSubscriptionTask(this);
        WidgetsUtils.Log.d(TAG,"start load credits");
        Utils.executeInMultiThread(getSubscriptionTask,accessToken,type);
    }

    @Override
    public void onRequestCompleted(List<Subscription> subscriptions) {
        if (progressView!=null)
            progressView.dismiss();
        for (Subscription subscription: subscriptions) {
            LinearLayout row = new LinearLayout(SVODActivity.this);
            row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            Button btnSubscription = new Button(SVODActivity.this,null,R.style.Light);
            btnSubscription = ViewUtils.setButtonDynamicStyle(btnSubscription,SVODActivity.this);
            btnSubscription.setId(subscription.getId());
            btnSubscription.setAllCaps(false);
            btnSubscription.setText(Html.fromHtml("<b><big>" + subscription.getName() + "</big></b>" +  "<br />" +
                    "<small style='font-size:x-small;'>" + subscription.getPrice() + "</small>"));
            final String productPriceId=subscription.getId().toString();
            final Subscription finalSubscription = subscription;
            Log.d(TAG+" type",type);
            btnSubscription.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(type.equals("1"))
                        sendRestRequest(productPriceId);
                    else{
                        sendMiddlewareRequest(finalSubscription);
                    }
                }
            });
            row.addView(btnSubscription);
            listButtonlayout.addView(row);
        }
    }

    @Override
    public void onRequestFail(Throwable throwable) {
        progressView.dismiss();
        Toast.makeText(SVODActivity.this,"Fail to get subscriptions",Toast.LENGTH_LONG).show();
        Log.e(TAG, throwable.getMessage(), throwable);
    }

    //Add subscriptions with cash
    private void sendRestRequest(String id) {
        PurchaseSubcriptionTask purchaseSubcriptionTask = new PurchaseSubcriptionTask();
        Utils.executeInMultiThread(purchaseSubcriptionTask,id);
    }

    private void sendMiddlewareRequest(Subscription subscription){
        PurchaseSubscriptionByCreditTask purchaseSubscriptionByCreditTask = new PurchaseSubscriptionByCreditTask();
        Utils.executeInMultiThread(purchaseSubscriptionByCreditTask,subscription);
    }
    //Call MPP Payment
    private class PurchaseSubcriptionTask extends AsyncTask<String, Void,String >{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressView.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                //http://api.danet.vn/subscriptions/13807/purchase
                url = new URL(API_URL + "subscriptions/" + params[0]+ "/purchase");
                conn = (HttpURLConnection) url.openConnection();
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
            }
            catch(Exception e){
                e.printStackTrace();
            }
            finally{
                conn.disconnect();
            }
            return "";
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
                    dialogView = new AddMessageDialogView(SVODActivity.this,"Có lỗi xảy ra",sb.toString() + ". Xin vui lòng quay lại sau.","Ok");
                    dialogView.show();
                }

            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    //Get
    private class PurchaseSubscriptionByCreditTask extends AsyncTask<Subscription,Void,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressView.show();
        }

        @Override
        protected String doInBackground(Subscription... params) {
            try{
                //http://api.danet.vn/subscriptions/14573/purchase/credits
                url = new URL(API_URL + "subscriptions/" + params[0].getId() + "/purchase/credits");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Movideo-Auth",accessToken);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                String body = "{\n" +
                        "\"return_url\":\"http://www.danet.vn/thanks\",\n" +
                        "\"payment\":\"credits\",\n" +
                        "\"service_id\":\""+ params[0].getService_id() +"\",\n" +
                        "\"id\":\""+ params[0].getId() +"\",\n" +
                        "\"object\":\"SVOD\",\n" +
                        "\"period\":"+ params[0].getPeriod() +",\n" +
                        "\"unit\":\""+ params[0].getUnit() +"\"\n" +
                        "}";
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
            }
            catch(Exception e){
                e.printStackTrace();
            }
            finally{
                conn.disconnect();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (progressView!=null)
                progressView.dismiss();
            StringBuilder sb = new StringBuilder();
            try {
                JSONObject mainObject = new JSONObject(s);
                if(mainObject.has("success")) {
                    sb.append("Đăng ký Phim gói thành công");
                    dialogView = new AddMessageDialogView(SVODActivity.this,sb.toString(),"Chúc bạn xem phim vui vẻ","Ok");
                    dialogView.show();
                }
                else {
                    JSONObject error = mainObject.getJSONObject("error");
                    String error_message = error.getString("message");
                    sb.append(error_message);
                    dialogView = new AddMessageDialogView(SVODActivity.this,"Có lỗi xảy ra",sb.toString(),"Ok");
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
