package com.movideo.whitelabel;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.movideo.baracus.model.user.User;
import com.movideo.whitelabel.communication.ContentRequestListener;
import com.movideo.whitelabel.communication.UpdateUserDetailsTask;
import com.movideo.whitelabel.util.Utils;
import com.movideo.whitelabel.util.ViewUtils;
import com.movideo.whitelabel.util.sharedPreference.PreferenceHelper;
import com.movideo.whitelabel.view.AddMessageDialogView;
import com.movideo.whitelabel.view.ProgressView;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.movideo.whitelabel.ContentHandler.API_URL;

/**
 * Created by BHD on 1/18/2017.
 */


public class SignupActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher, View.OnTouchListener {

    @Bind(R.id.closeButton)
    Button closeButton;
    @Bind(R.id.firstNameEditText)
    EditText firstNameEditText;
    @Bind(R.id.lastNameEditText)
    EditText lastNameEditText;
    @Bind(R.id.emailEditText)
    EditText emailEditText;
    @Bind(R.id.phoneEditText)
    EditText phoneEditText;
    @Bind(R.id.birthdayEditText)
    EditText birthdayEditText;
    @Bind(R.id.passwordEditText)
    EditText passwordEditText;
    @Bind(R.id.passwordReenterEditText)
    EditText passwordReenterEditText;
    @Bind(R.id.checkBox)
    CheckBox checkBox;
    @Bind(R.id.buttonSignUp)
    Button buttonSignUp;


    private ProgressView progressView;
    private View focusView;
    private boolean firstNameValid;
    private boolean lastNameValid;
    private boolean emailValid;
    private boolean phoneNumberValid;
    private boolean birthdayValid;
    private boolean passwordValid;
    private boolean passwordReenterValid;
    private boolean checkBoxValid;

    private Calendar myCalendar = Calendar.getInstance();

    private String TAG = getClass().getSimpleName();
    private HttpURLConnection conn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_screen);
        ButterKnife.bind(this);

        buttonSignUp.setEnabled(false);
        buttonSignUp.setOnClickListener(this);
        closeButton.setOnClickListener(this);
        birthdayEditText.setOnClickListener(this);
        checkBox.setOnClickListener(this);
        progressView = new ProgressView(this);
        loadInputEventListener();
        focusView = findViewById(R.id.singupTextView);

        firstNameValid = false;
        lastNameValid = false;
        emailValid = false;
        phoneNumberValid = false;
        birthdayValid = false;
        passwordValid = false;
        passwordReenterValid = false;
        checkBoxValid = false;

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
    protected void onStop() {
        if(progressView!=null)
            progressView.dismiss();
        super.onStop();
    }

    public boolean isPhoneNumberValid(String phoneNumber){
        String regExpn ="^[+]?[0-9]{9,13}$";

        CharSequence inputStr = phoneNumber;

        Pattern pattern = Pattern.compile(regExpn,Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if(matcher.matches())
            return true;
        else
            return false;
    }

    public  boolean isBirthdayValid(String birthDay){
        String regExpn = "^(?:(?:31(\\/)(?:0?[13578]|1[02]))\\1|(?:(?:29|30)(\\/)(?:0?[1,3-9]|1[0-2])\\2))(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$|^(?:29(\\/)0?2\\3(?:(?:(?:1[6-9]|[2-9]\\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))$|^(?:0?[1-9]|1\\d|2[0-8])(\\/)(?:(?:0?[1-9])|(?:1[0-2]))\\4(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$";
        CharSequence inputStr = birthDay;

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
            case R.id.buttonSignUp:
                Log.d("SIGN UP", "YES");
                signup(firstNameEditText.getText().toString(),
                        lastNameEditText.getText().toString(),
                        emailEditText.getText().toString(),
                        phoneEditText.getText().toString(),
                        birthdayEditText.getText().toString(),
                        passwordEditText.getText().toString(),
                        passwordReenterEditText.getText().toString());
                break;
            case R.id.checkBox :
                setEnabledButton();
                break;
            case R.id.birthdayEditText:
                Log.d("clicked","click");
                DatePickerDialog datePickerDialog = new DatePickerDialog(this,android.R.style.Theme_Holo_Light_Dialog_NoActionBar, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setFocusable(false);
                datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
                datePickerDialog.getDatePicker().setCalendarViewShown(false);
                datePickerDialog.getDatePicker().setSpinnersShown(true);
                datePickerDialog.show();
                break;
            default:
                break;

        }
    }

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            Log.d("CALENDAR",String.valueOf(monthOfYear));
            updateLabel();
        }

    };

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        focusView = v;
        return false;
    }

    private void updateLabel() {

        //String myFormat = "dd/mm/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Log.d("BEFORE FORMAT",myCalendar.getTime().toString());
        Log.d("AFTER FORMAT",sdf.format(myCalendar.getTime()));
        birthdayEditText.setText(sdf.format(myCalendar.getTime()));
    }

    private void loadInputEventListener(){
        firstNameEditText.setOnTouchListener(this);
        lastNameEditText.setOnTouchListener(this);
        passwordEditText.setOnTouchListener(this);
        passwordReenterEditText.setOnTouchListener(this);
        phoneEditText.setOnTouchListener(this);
        birthdayEditText.setOnTouchListener(this);
        emailEditText.setOnTouchListener(this);
        firstNameEditText.addTextChangedListener(this);
        lastNameEditText.addTextChangedListener(this);
        emailEditText.addTextChangedListener(this);
        phoneEditText.addTextChangedListener(this);
        passwordReenterEditText.addTextChangedListener(this);
        birthdayEditText.addTextChangedListener(this);
        passwordEditText.addTextChangedListener(this);

    }

    public void setEnabledButton(){
        if ( !firstNameEditText.getText().toString().isEmpty())
            firstNameValid = true;
        else
            firstNameValid = false;
        if ( !lastNameEditText.getText().toString().isEmpty())
            lastNameValid = true;
        else
            lastNameValid = false;
        if ( passwordEditText.getText().toString().isEmpty())
            passwordValid = false;
        if(checkBox.isChecked())
            checkBoxValid = true;
        else
            checkBoxValid = false;
        if(isEmailValid(emailEditText.getText().toString()))
            emailValid = true;
        else
            emailValid = false;
        if(isBirthdayValid(birthdayEditText.getText().toString()))
            birthdayValid = true;
        else
            birthdayValid = false;
        if(isPhoneNumberValid(phoneEditText.getText().toString()))
            phoneNumberValid = true;
        else
            phoneNumberValid = false;
        if(passwordReenterEditText.getText().toString().isEmpty())
            passwordReenterValid = false;
        if (firstNameValid &&
                lastNameValid &&
                birthdayValid &&
                emailValid &&
                phoneNumberValid &&
                passwordValid &&
                passwordReenterValid &&
                checkBoxValid){
            buttonSignUp.setEnabled(true);
            buttonSignUp.setBackgroundResource(R.drawable.enable_button_bg);
        }
        else {
            buttonSignUp.setEnabled(false);
            buttonSignUp.setBackgroundResource(R.drawable.login_button_bg);
        }
        Log.d("firstNameValid", String.valueOf(firstNameValid));
        Log.d("lastNameValid", String.valueOf(lastNameValid));
        Log.d("birthdayValid", String.valueOf(birthdayValid));
        Log.d("emailValid", String.valueOf(emailValid));
        Log.d("phoneNumberValid", String.valueOf(phoneNumberValid));
        Log.d("passwordValid", String.valueOf(passwordValid));
        Log.d("passwordReenterValid", String.valueOf(passwordReenterValid));
        Log.d("checkBoxValid", String.valueOf(checkBoxValid));

        Log.d(TAG, String.valueOf(buttonSignUp.isEnabled()));

    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String text = s.toString();
        switch(focusView.getId()){
            case R.id.emailEditText:
                if(!isEmailValid(text)){
                    if(text.isEmpty())
                        focusView.setBackgroundResource(R.drawable.edit_text_bg);
                    else
                        focusView.setBackgroundResource(R.drawable.border_red);
                    emailValid = false;
                }
                else {
                    focusView.setBackgroundResource(R.drawable.border_green);
                    emailValid = true;
                }
                setEnabledButton();
                break;
            case R.id.phoneEditText:
                if(!isPhoneNumberValid(text)){
                    if(text.isEmpty())
                        focusView.setBackgroundResource(R.drawable.edit_text_bg);
                    else
                        focusView.setBackgroundResource(R.drawable.border_red);
                    phoneNumberValid = false;

                }
                else {
                    focusView.setBackgroundResource(R.drawable.border_green);
                    phoneNumberValid = true;
                }
                setEnabledButton();
                break;
            case R.id.passwordReenterEditText:

                //reenterpassword not null & smaller than 6
                if(text.length()>0 && text.length()<6){
                    focusView.setBackgroundResource(R.drawable.border_red);
                    passwordReenterValid = false;
                }
                // reenterpassword null
                else if(text.isEmpty()){
                    focusView.setBackgroundResource(R.drawable.edit_text_bg);
                    passwordReenterValid =false;
                }
                //reenterpassword > 6
                else {
                    //reenterpassword =/= password
                    if (!text.equals(passwordEditText.getText().toString())) {
                        focusView.setBackgroundResource(R.drawable.border_red);
                        passwordReenterValid = false;
                    }
                    // reenetrpassword == password
                    else {
                        focusView.setBackgroundResource(R.drawable.border_green);
                        passwordReenterValid = true;
                    }
                }
                setEnabledButton();
                break;
            case R.id.passwordEditText:
                //password not null and smaller than 6
                if(text.length()>0 && text.length()<6){
                    passwordEditText.setBackgroundResource(R.drawable.border_red);
                    passwordValid = false;
                }
                //password null
                else if (text.length()==0){
                    passwordEditText.setBackgroundResource(R.drawable.edit_text_bg);
                    //reenterpassword null
                    if (text.equals(passwordReenterEditText.getText().toString())) {
                            passwordReenterEditText.setBackgroundResource(R.drawable.edit_text_bg);
                    }
                    //reenterpassword not null
                    else {
                        passwordReenterEditText.setBackgroundResource(R.drawable.border_red);
                        passwordReenterValid = false;
                    }
                }
                //password > 6
                else{
                    passwordEditText.setBackgroundResource(R.drawable.border_green);
                    passwordValid = true;
                    //password = reenterpassword
                    if (text.equals(passwordReenterEditText.getText().toString())) {
                        passwordReenterEditText.setBackgroundResource(R.drawable.border_green);
                        passwordReenterValid = true;
                    }
                    //reenterpassword =/= password
                    else {
                        if(passwordReenterEditText.getText().length()==0)
                            passwordReenterEditText.setBackgroundResource(R.drawable.edit_text_bg);
                        else
                            passwordReenterEditText.setBackgroundResource(R.drawable.border_red);
                        passwordReenterValid = false;
                    }
                }
                setEnabledButton();
                break;
            case R.id.birthdayEditText:
                if(!isBirthdayValid(text)){
                    if(text.isEmpty())
                        focusView.setBackgroundResource(R.drawable.edit_text_bg);
                    else
                        focusView.setBackgroundResource(R.drawable.border_red);
                    birthdayValid = false;
                }
                else {
                    focusView.setBackgroundResource(R.drawable.border_green);
                    birthdayValid = true;

                }
                setEnabledButton();
                break;
            case R.id.firstNameEditText:
                setEnabledButton();
                break;
            case R.id.lastNameEditText:
                setEnabledButton();
                break;
        }
    }

    private void signup(String firstname, String lastname, String email, String phone, String birthday, String password, String passwordReeenter){
        CreateAccountTask createAccountTask = new CreateAccountTask();
        Utils.executeInMultiThread(createAccountTask, firstname, lastname, email, phone, birthday,password,passwordReeenter);
    }

    private class CreateAccountTask extends AsyncTask<String, Void,String >
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressView.show();
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                //http://api.danet.vn/user
                URL url = new URL(API_URL + "user");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                String body = "{\"provider\":\"movideo\",\n" +
                        "\"given_name\":\""+ params[0] + "\",\n" +
                        "\"family_name\":\"" + params[1] + "\",\n" +
                        "\"identifier\":\"" + params[2] + "\",\n" +
                        "\"phone\":\"" + params[3] + "\",\n" +
                        "\"date_of_birth\":\"" + params[4]+ "\",\n" +
                        "\"password\":\"" + params[5] + "\",\n" +
                        "\"password_confirmation\":\""+ params[6] + "\"\n" +
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
                AddMessageDialogView dialogView;
                JSONObject mainObject = new JSONObject(s);
                if(mainObject.has("error")) {
                    JSONObject error = mainObject.getJSONObject("error");
                    String error_message = error.getString("system_message");
                    sb.append(error_message);
                    dialogView = new AddMessageDialogView(SignupActivity.this,"Không thể đăng ký",sb.toString(),"Ok");
                    dialogView.show();
                }
                else {
                    if(mainObject.has("accessToken")){
                        UserLoginParser userLoginParser = new UserLoginParser();
                        ViewUtils.execute(userLoginParser, new String[]{mainObject.getString("accessToken")});
                        dialogView = new AddMessageDialogView(SignupActivity.this,"Đăng ký thành công","Bạn sẽ được chuyển đến trang chủ trong giây lát","Ok");
                        dialogView.show();
                    }
                }


            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public class UserLoginParser extends AsyncTask<String, String, User> {

        private String error = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressView.show();
        }

        @Override
        protected User doInBackground(String... params) {
            ContentHandler contentHandler = ContentHandler.getInstance();
            User user = null;
            try {
                user = contentHandler.getUser(params[0]);
                user.setAccessToken(params[0]);

            } catch (Exception e) {
                error = e.getMessage();
            }
            return user;
        }

        @Override
        protected void onPostExecute(User user) {
            super.onPostExecute(user);

            if (error == null && user != null) {
                boolean updated = false;
                if (user.getIdentifier() == null || user.getIdentifier().isEmpty()) {
                    user.setIdentifier(emailEditText.getText().toString().trim());
                    updated = true;
                }
                if (user.getPassword() == null || user.getPassword().isEmpty()) {
                    user.setPassword(passwordEditText.getText().toString().trim());
                }
                if ((user.getGivenName() == null || user.getGivenName().isEmpty()) && (user.getFamilyName() == null || user.getFamilyName().isEmpty())) {
                    user.setGivenName(firstNameEditText.getText().toString().trim());
                    user.setFamilyName(lastNameEditText.getText().toString().trim());
                    updated = true;
                }

                if (updated) {
                    UpdateUserDetailsTask updateUserDetailsTask = new UpdateUserDetailsTask(new ContentRequestListener<User>() {
                        @Override
                        public void onRequestCompleted(User user) {
                            WhiteLabelApplication whiteLabelApplication = WhiteLabelApplication.getInstance();
                            whiteLabelApplication.setUser(user);
                            whiteLabelApplication.setIsUserLoggedIn(true);
                            whiteLabelApplication.setAccessToken(user.getAccessToken());

                            PreferenceHelper.setDataInSharedPreference(SignupActivity.this, getResources().getString(R.string.user_info), user);
                            PreferenceHelper.setDataInSharedPreference(SignupActivity.this, getResources().getString(R.string.user_is_logged_in), true);
                            if (progressView!=null)
                                progressView.dismiss();
                            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            SignupActivity.this.finish();
                            Toast.makeText(SignupActivity.this, getResources().getString(R.string.user_logged_in_msg), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onRequestFail(Throwable throwable) {
                            Log.d("UpdateUserDetailTask", "FAILLL");
                            if (progressView!=null)
                                progressView.dismiss();
                            AddMessageDialogView dialogView = new AddMessageDialogView(SignupActivity.this, getString(R.string.label_sign_in_unable), getString(R.string.dialog_msg_error_occurred), getString(R.string.label_ok));
                            dialogView.show();
                        }
                    });
                    Utils.executeInMultiThread(updateUserDetailsTask, user);
                } else {

                    WhiteLabelApplication whiteLabelApplication = WhiteLabelApplication.getInstance();
                    whiteLabelApplication.setUser(user);
                    whiteLabelApplication.setIsUserLoggedIn(true);
                    whiteLabelApplication.setAccessToken(user.getAccessToken());

                    PreferenceHelper.setDataInSharedPreference(SignupActivity.this, getResources().getString(R.string.user_info), user);
                    PreferenceHelper.setDataInSharedPreference(SignupActivity.this, getResources().getString(R.string.user_is_logged_in), true);
                    if (progressView!=null)
                        progressView.dismiss();
                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    SignupActivity.this.finish();
                    Toast.makeText(SignupActivity.this, getResources().getString(R.string.user_logged_in_msg), Toast.LENGTH_LONG).show();
                }
            } else {
                Log.d("UserLoginParser", "FAILLL");
                if (progressView!=null)
                    progressView.dismiss();
                AddMessageDialogView dialogView = new AddMessageDialogView(SignupActivity.this, getString(R.string.label_sign_in_unable), getString(R.string.dialog_msg_error_occurred), getString(R.string.label_ok));
                dialogView.show();
            }
        }
    }
}
