package com.movideo.whitelabel;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.movideo.baracus.model.common.Image;
import com.movideo.whitelabel.util.Utils;
import com.movideo.whitelabel.util.ViewUtils;
import com.movideo.whitelabel.view.AddMessageDialogView;
import com.movideo.whitelabel.view.ProgressView;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.movideo.whitelabel.ContentHandler.API_URL;

/**
 * Created by BHD on 1/20/2017.
 */

public class ResetPaswordActivity extends AppCompatActivity implements View.OnClickListener {

    @Bind(R.id.viewSwitcher)
    ViewSwitcher mViewSwitcher;
    @Bind(R.id.closeImageButton)
    ImageButton closeImageButton;
    @Bind(R.id.closeButton)
    Button closeButton;
    @Bind(R.id.emailEditText)
    EditText emailEditText;
    @Bind(R.id.sendResetPasswordButton)
    Button sendResetPasswordButton;
    @Bind(R.id.imageEmailView)
    ImageView imageEmailView;
    @Bind(R.id.alertEmailTextView)
    TextView alertEmailTitleTextView;
    @Bind(R.id.alertDescriptionTextView)
    TextView alertEmailDescriptionTextView;
    @Bind(R.id.okButton)
    Button okButton;

    private ProgressView progressView;
    private View focusView;
    private String TAG = getClass().getSimpleName();
    private HttpURLConnection conn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);
        ButterKnife.bind(this);

        sendResetPasswordButton.setEnabled(false);
        closeButton.setOnClickListener(this);
        sendResetPasswordButton.setOnClickListener(this);
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = emailEditText.getText().toString();
                if(!isEmailValid(text)){
                    if(text.isEmpty())
                        emailEditText.setBackgroundResource(R.drawable.edit_text_bg);
                    else
                        emailEditText.setBackgroundResource(R.drawable.border_red);
                    sendResetPasswordButton.setEnabled(false);
                    sendResetPasswordButton.setBackgroundResource(R.drawable.login_button_bg);
                }
                else {
                    emailEditText.setBackgroundResource(R.drawable.border_green);
                    sendResetPasswordButton.setEnabled(true);
                    sendResetPasswordButton.setBackgroundResource(R.drawable.enable_button_bg);
                }
            }
        });
        progressView = new ProgressView(this);


    }

    public boolean isEmailValid(String email)
    {
        String regExpn =
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                        +"((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        +"([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                        +"([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(regExpn,Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if(matcher.matches())
            return true;
        else
            return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.closeButton:
                onBackPressed();
                break;
            case R.id.sendResetPasswordButton:
                ForgotPasswordParser forgotPasswordParser = new ForgotPasswordParser();
                Utils.executeInMultiThread(forgotPasswordParser,emailEditText.getText().toString().trim());
                break;
        }
    }

    public class ForgotPasswordParser extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressView.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try{
                URL url = new URL(API_URL + "user/resetpassword");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                String body = "{\"identifier\":\""+ params[0]+ "\"}";
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
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (progressView!=null)
                progressView.dismiss();
            mViewSwitcher.showNext();
            StringBuilder sb = new StringBuilder();
            try {
                AddMessageDialogView dialogView;
                JSONObject mainObject = new JSONObject(result);
                if(mainObject.has("success")) {
                    boolean isSuccess = mainObject.getBoolean("success");
                    if(isSuccess){
                        imageEmailView.setColorFilter(Color.parseColor("#81b843"));
                        alertEmailTitleTextView.setText("Vui lòng kiểm tra hòm thư");
                        alertEmailDescriptionTextView.setText(Html.fromHtml("Chúng tôi đã gửi thông tin mật khẩu cho bạn vào hòm thư<font color='#9aca3c'> "+ emailEditText.getText().toString() +"</font>, thư sẽ tới trong khoảng 5 phút."));
                        okButton.setText("Đồng ý");
                        okButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        });
                    }
                    else{
                        imageEmailView.setColorFilter(Color.parseColor("#FF0000"));
                        alertEmailTitleTextView.setText("Không thể phục hồi mật khẩu");
                        alertEmailDescriptionTextView.setTextColor(Color.WHITE);
                        alertEmailDescriptionTextView.setText("Có lỗi xảy ra trong quá trình phục hồi mật khẩu. Vui lòng kiểm tra lại địa chỉ email hoặc thử lại sau");
                        okButton.setText("Quay lại");
                        okButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mViewSwitcher.showPrevious();
                            }
                        });
                    }
                }
                else {

                }


            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
