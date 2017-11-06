package com.movideo.whitelabel;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.movideo.baracus.model.product.Product;
import com.movideo.baracus.model.user.User;
import com.movideo.whitelabel.communication.AuthenticationParser;
import com.movideo.whitelabel.communication.AuthenticationUserResultReceived;
import com.movideo.whitelabel.communication.ContentRequestListener;
import com.movideo.whitelabel.communication.FacebookAuthenticationParser;
import com.movideo.whitelabel.communication.UpdateUserDetailsTask;
import com.movideo.whitelabel.util.DialogEventListeners;
import com.movideo.whitelabel.util.NetworkAvailability;
import com.movideo.whitelabel.util.Utils;
import com.movideo.whitelabel.util.ViewUtils;
import com.movideo.whitelabel.util.sharedPreference.PreferenceHelper;
import com.movideo.whitelabel.view.AddMessageDialogView;
import com.movideo.whitelabel.view.AddTwoButtonDialogView;
import com.movideo.whitelabel.view.ProgressView;

import org.json.JSONObject;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    @Bind(R.id.closeImageButton)
    ImageButton closeImageButton;
    @Bind(R.id.emailEditText)
    EditText emailEditText;
    @Bind(R.id.passwordEditText)
    EditText passwordEditText;
    @Bind(R.id.loginButton)
    Button loginButton;
    @Bind(R.id.facebookLoginButton)
    LoginButton facebookLoginButton;
    @Bind(R.id.forgotPasswordTextView)
    TextView forgotPasswordTextView;

    @Bind(R.id.createAccountTextView)
    TextView createAccountTextView;

    private CallbackManager callbackManager;

    private ProgressView progressView;
    private String userNameFB;
    private final String SITE_URL = "http://www.danet.vn/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        setContentView(R.layout.activity_login_screen);
        ButterKnife.bind(this);

        if (ViewUtils.isTablet(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }


        facebookLoginButton = (LoginButton) findViewById(R.id.facebookLoginButton);
        facebookLoginButton.setReadPermissions(Arrays.asList("public_profile, email"));

        callbackManager = CallbackManager.Factory.create();

        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                userAuthenticate("facebook", loginResult.getAccessToken().getToken().toString(), "");
                LoginManager.getInstance().logOut();

//                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
//                    @Override
//                    public void onCompleted(JSONObject user, GraphResponse graphResponse) {
//                        userNameFB = user.optString("name");
//                        userAuthenticate("facebook", user.optString("email"), "");
//                        LoginManager.getInstance().logOut();
//                    }
//                });
//                Bundle parameters = new Bundle();
//                parameters.putString("fields", "name,email");
//                request.setParameters(parameters);
//                request.executeAsync();
            }

            @Override
            public void onCancel() {
//                Toast.makeText(LoginActivity.this, getResources().getString(R.string.facebook_login_cancelled), Toast.LENGTH_SHORT).show();
                loadLoinErrorDialog(getString(R.string.facebook_login_cancelled));
            }

            @Override
            public void onError(FacebookException e) {
//                Toast.makeText(LoginActivity.this, getResources().getString(R.string.facebook_login_failed), Toast.LENGTH_SHORT).show();
                loadLoinErrorDialog(getString(R.string.facebook_login_failed));
            }
        });

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    login();
                    return true;
                }
                return false;
            }
        });

        closeImageButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);
        forgotPasswordTextView.setOnClickListener(this);
        createAccountTextView.setOnClickListener(this);

        progressView = new ProgressView(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.closeImageButton:
                onBackPressed();
                break;
            case R.id.loginButton:
                login();
                break;
            case R.id.forgotPasswordTextView:

                if (NetworkAvailability.chkStatus(this)) {
                    /*
                    AddTwoButtonDialogView dialogView = new AddTwoButtonDialogView(this, "Quên mật khẩu?", this.getString(R.string.dialog_msg_take_to_web), this.getString(R.string.label_go_to_website), this.getString(R.string.label_cancel), new DialogEventListeners() {
                        @Override
                        public void onPositiveButtonClick(DialogInterface dialog) {

                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(SITE_URL + "go?action=forgot-password"));
                            startActivity(intent);
                            dialog.dismiss();
                        }

                        @Override
                        public void onNegativeButtonClick(DialogInterface dialog) {
                        }
                    });
                    dialogView.show();
                    */

                    // Added by Anh Tuan
                    Intent intent = new Intent(LoginActivity.this, ResetPaswordActivity.class);
                    //intent.putExtra("url", "http://tools.danet.vn/account/forgotpass.php");
                    startActivity(intent);

                } else {
                    loadLoinErrorDialog(getString(R.string.dialog_msg_network_error_occurred));
                }
                break;
            case R.id.createAccountTextView:
                if (NetworkAvailability.chkStatus(this)) {

                    /*
                    AddTwoButtonDialogView dialogView = new AddTwoButtonDialogView(this, "Tạo tài khoản?", this.getString(R.string.dialog_msg_take_to_web), this.getString(R.string.label_go_to_website), this.getString(R.string.label_cancel), new DialogEventListeners() {
                        @Override
                        public void onPositiveButtonClick(DialogInterface dialog) {

                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(SITE_URL + "go?action=create-account"));
                            startActivity(intent);
                            dialog.dismiss();
                        }

                        @Override
                        public void onNegativeButtonClick(DialogInterface dialog) {
                        }
                    });
                    dialogView.show();
                    */
                    // Added by Anh Tuan
                    //Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                    Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                    //intent.putExtra("url", "http://tools.danet.vn/account/register.php");
                    startActivity(intent);

                } else {
                    loadLoinErrorDialog(getString(R.string.dialog_msg_network_error_occurred));
                }
                break;
            default:
                break;
        }
    }

    private void login() {
        if (NetworkAvailability.chkStatus(this)) {
            if (isLoginValidation()) {
                userNameFB = "";
                userAuthenticate("movideo", emailEditText.getText().toString().trim(), passwordEditText.getText().toString().trim());
            }
        } else {
            loadLoinErrorDialog(getString(R.string.dialog_msg_network_error_occurred));
        }
    }

    private boolean isLoginValidation() {
        if (TextUtils.isEmpty(emailEditText.getText().toString().trim()) && TextUtils.isEmpty(passwordEditText.getText().toString().trim())) {
            Toast.makeText(this, getResources().getString(R.string.email_and_password_blank), Toast.LENGTH_LONG).show();
            return false;
        } else if (TextUtils.isEmpty(emailEditText.getText().toString().trim())) {
            Toast.makeText(this, getResources().getString(R.string.email_address_blank), Toast.LENGTH_LONG).show();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText().toString().trim()).matches()) {
            Toast.makeText(this, getResources().getString(R.string.email_address_invalid), Toast.LENGTH_LONG).show();
            return false;
        } else if (TextUtils.isEmpty(passwordEditText.getText().toString().trim())) {
            Toast.makeText(this, getResources().getString(R.string.password_blank), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /*creating auth token after that fetch user information*/
    private void userAuthenticate(String provider, String identifier, String password) {

        AuthenticationUserResultReceived resultReceived = new AuthenticationUserResultReceived() {
            @Override
            public void onResult(String error, User result) {
                if (error == null && result != null) {
                    UserLoginParser userLoginParser = new UserLoginParser();
                    ViewUtils.execute(userLoginParser, new String[]{result.getAccessToken()});
                } else {
                    if (error != null && error.equals(getResources().getString(R.string.un_authorised_exception_code)))
                        loadLoinErrorDialog(getString(R.string.dialog_msg_wrong_username_password));
                    else if (error != null && error.equals(getResources().getString(R.string.not_found_exception_code)))
                        loadLoinErrorDialog(getString(R.string.dialog_msg_wrong_username_password));
                    else
                        loadLoinErrorDialog(getString(R.string.dialog_msg_error_occurred));
                }
            }
        };


        AsyncTask<String, String, User> asyncTask;

        if (provider == "facebook")
        {
            String[] params = {provider, "", "", identifier};
            asyncTask = new FacebookAuthenticationParser(this, resultReceived);
            ViewUtils.execute(asyncTask, params);
        }
        else {
            String[] params = {provider, identifier, password};
            asyncTask = new AuthenticationParser(this, resultReceived );
            ViewUtils.execute(asyncTask, params);
        }


    }

    private void loadLoinErrorDialog(String message) {

        AddMessageDialogView dialogView = new AddMessageDialogView(this, getString(R.string.label_sign_in_unable), message, getString(R.string.label_ok));
        dialogView.show();
    }

    /*User login process for facebook/custom login*/
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
                    if (userNameFB.contains(" ")) {
                        String[] names = userNameFB.split(" ");

                        user.setGivenName(names[0]);
                        user.setFamilyName(names[names.length - 1]);
                    } else {
                        user.setGivenName(userNameFB);
                    }
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

                            PreferenceHelper.setDataInSharedPreference(LoginActivity.this, getResources().getString(R.string.user_info), user);
                            PreferenceHelper.setDataInSharedPreference(LoginActivity.this, getResources().getString(R.string.user_is_logged_in), true);
                            if (progressView!=null)
                                progressView.dismiss();
                            LoginActivity.this.finish();
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.user_logged_in_msg), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onRequestFail(Throwable throwable) {
                            if (progressView!=null)
                                progressView.dismiss();
                            loadLoinErrorDialog(getString(R.string.dialog_msg_error_occurred));
                        }
                    });
                    Utils.executeInMultiThread(updateUserDetailsTask, user);
                } else {

                    WhiteLabelApplication whiteLabelApplication = WhiteLabelApplication.getInstance();
                    whiteLabelApplication.setUser(user);
                    whiteLabelApplication.setIsUserLoggedIn(true);
                    whiteLabelApplication.setAccessToken(user.getAccessToken());

                    PreferenceHelper.setDataInSharedPreference(LoginActivity.this, getResources().getString(R.string.user_info), user);
                    PreferenceHelper.setDataInSharedPreference(LoginActivity.this, getResources().getString(R.string.user_is_logged_in), true);
                    if (progressView!=null)
                        progressView.dismiss();
                    LoginActivity.this.finish();
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.user_logged_in_msg), Toast.LENGTH_LONG).show();
                }
            } else {
                if (progressView!=null)
                    progressView.dismiss();
                loadLoinErrorDialog(getString(R.string.dialog_msg_error_occurred));
            }
        }
    }

    /*User forgot password process*/
    public class ForgotPasswordParser extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressView.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String error = null;
            ContentHandler contentHandler = ContentHandler.getInstance();
            try {
                contentHandler.resetPassword(null, params[0]);
            } catch (Exception e) {
                error = e.getMessage();
            }
            return error;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (progressView!=null)
                progressView.dismiss();

        }
    }
}
