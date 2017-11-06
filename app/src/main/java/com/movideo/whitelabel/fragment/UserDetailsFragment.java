package com.movideo.whitelabel.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.movideo.baracus.model.user.User;
import com.movideo.whitelabel.BarcodeCaptureActivity;
import com.movideo.whitelabel.ContentActivity;
import com.movideo.whitelabel.ContentHandler;
import com.movideo.whitelabel.ExtraActivity;
import com.movideo.whitelabel.MainActivity;
import com.movideo.whitelabel.R;
import com.movideo.whitelabel.SVODActivity;
import com.movideo.whitelabel.TVODActivity;
import com.movideo.whitelabel.TransactionHistoryActivity;
import com.movideo.whitelabel.WhiteLabelApplication;
import com.movideo.whitelabel.communication.AuthenticationParser;
import com.movideo.whitelabel.communication.AuthenticationUserResultReceived;
import com.movideo.whitelabel.util.DialogEventListeners;
import com.movideo.whitelabel.util.DisplayResources;
import com.movideo.whitelabel.util.NetworkAvailability;
import com.movideo.whitelabel.util.ViewUtils;
import com.movideo.whitelabel.util.sharedPreference.PreferenceHelper;
import com.movideo.whitelabel.view.AddTwoButtonDialogView;
import com.movideo.whitelabel.view.ProgressView;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UserDetailsFragment extends Fragment implements View.OnClickListener, DialogEventListeners {


    private static final String RENEW_SUBSCRIPTION_SITE_URL = "http://www.danet.vn/subscribe";
    private static final String BUY_CREDITS_SITE_URL = "http://www.danet.vn/buy/credits";
    private static final String PRIVACY_POLICY_SITE_URL = "http://www.danet.vn/content/privacy-policy.html";
    private static final String HELP_SITE_URL = "http://www.danet.vn/content/help.html";
    private static final String TERMS_SITE_URL = "http://www.danet.vn/content/terms.html";
    private static final String CREATE_ACCOUNT_SITE_URL = "http://www.danet.vn/go?action=create-account";
    private static final String TAG = UserDetailsFragment.class.getSimpleName();
    private SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

    //    @Bind(R.id.userImageView)
//    ImageView userImageView;
    @Bind(R.id.userNameTextView)
    TextView userNameTextView;
    @Bind(R.id.memberShipTextView)
    TextView memberShipTextView;
    @Bind(R.id.renewButtonTextView)
    TextView renewButtonTextView;
    @Bind(R.id.cineplexCreditCountTextView)
    TextView cineplexCreditCountTextView;
    @Bind(R.id.transactionHistoryButton)
    Button transactionHistoryButton;
    @Bind(R.id.renewBuffetButton)
    Button renewBuffetButton;
    @Bind(R.id.cineplexButton)
    Button cineplexButton;
    @Bind(R.id.voucherButton)
    Button voucherButton;
    @Bind(R.id.activateTVButton)
    Button activateTVButton;
    @Bind(R.id.signOutButton)
    Button signOutButton;
    @Bind(R.id.privacyPolicyTextView)
    TextView privacyPolicyTextView;
    @Bind(R.id.termTextView)
    TextView termTextView;
    @Bind(R.id.helpTextView)
    TextView helpTextView;

    private int clickedId;
    private User user = null;
    private DisplayResources displayResources;
    private ProgressView progressView;
    private AddTwoButtonDialogView dialogView;
    private AddTwoButtonDialogView renewSubscriptionDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        displayResources = new DisplayResources(getActivity());
//        user = PreferenceHelper.getSharedPrefData(getActivity(), getActivity().getResources().getString(R.string.user_info), User.class);
        user = WhiteLabelApplication.getInstance().getUser();
        progressView = new ProgressView(getActivity());
        dialogView = new AddTwoButtonDialogView(getContext(), getString(R.string.dialog_msg_take_to_web), null, getString(R.string.label_go_to_website), getString(R.string.label_cancel), this);
        //renewSubscriptionDialog();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_details, container, false);
        ButterKnife.bind(this, view);
        helpTextView.setOnClickListener(this);
        termTextView.setOnClickListener(this);
        privacyPolicyTextView.setOnClickListener(this);
        transactionHistoryButton.setOnClickListener(this);
        renewBuffetButton.setOnClickListener(this);
        signOutButton.setOnClickListener(this);
        cineplexButton.setOnClickListener(this);
        voucherButton.setOnClickListener(this);
        activateTVButton.setOnClickListener(this);
        renewButtonTextView.setOnClickListener(this);
        /*set all widgets/view values*/
        setWidgetsValues();

        if (NetworkAvailability.chkStatus(getActivity())) {
            UserDetailUpdateParser userDetailUpdateParser = new UserDetailUpdateParser();
            ViewUtils.execute(userDetailUpdateParser, WhiteLabelApplication.getInstance().getAccessToken());
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (NetworkAvailability.chkStatus(getActivity())) {
//            UserDetailUpdateParser userDetailUpdateParser = new UserDetailUpdateParser();
//            ViewUtils.execute(userDetailUpdateParser, WhiteLabelApplication.getInstance().getAccessToken());
//        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(progressView!=null){
            progressView.dismiss();
        }
    }

    /*set all widgets/view values*/
    private void setWidgetsValues() {
//        if (user.getAvatar() != null && !user.getAvatar().equals("")) {
//            Picasso.with(getActivity())
//                    .load(user.getAvatar())
//                    .resize(displayResources.dpToPx((int) getActivity().getResources().getDimension(R.dimen.user_detail_user_icon_width)), displayResources.dpToPx((int) getActivity().getResources().getDimension(R.dimen.user_detail_user_icon_height)))
//                    .into(userImageView);
//        }
        if (user.getGivenName() != null && !user.getGivenName().equals("")) {
            userNameTextView.setText(user.getGivenName());
        }
        if (user.getFamilyName() != null && !user.getFamilyName().equals("")) {
            userNameTextView.append(" " + user.getFamilyName());
        }

        if (user.getCreated() != null) {
            memberShipTextView.setText("Member since ");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM ,yyyy");
            memberShipTextView.append(simpleDateFormat.format(user.getCreated()));
        }

        if (user.getCredits() != null && user.getCredits() > 0) {
            cineplexCreditCountTextView.setText(String.valueOf(user.getCredits()));
        } else {
            cineplexCreditCountTextView.setText("0");
        }

        if (checkUserSubscribed(user)) {
            renewButtonTextView.setText("Hạn phim gói: "+ format.format(user.getSubscription().getEndDate()));
            renewButtonTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_subscribed, 0, 0);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.transactionHistoryButton:
                intent = new Intent(getActivity(),TransactionHistoryActivity.class );
                startActivity(intent);
                break;
            case R.id.renewBuffetButton:
                if(checkUserSubscribed(user)) {
                    String text = "Thời hạn đăng ký phim gói đến " + format.format(user.getSubscription().getEndDate());
                    renewSubscriptionDialog = new AddTwoButtonDialogView(getContext(),"Đã đăng ký phim gói",text,null,"Ok",this);
                    renewSubscriptionDialog.show();
                }
                else {
                    renewSubscriptionDialog = new AddTwoButtonDialogView(getContext(), "Thanh toán phim gói", null, "Bằng tiền", "Bằng điểm", this);
                    renewSubscriptionDialog.show();
                }
                break;
            case R.id.cineplexButton:
                renewSubscriptionDialog = new AddTwoButtonDialogView(getContext(), "Nạp điểm DANET", null, "Thẻ cào ĐT", "Bằng tiền", this);
                renewSubscriptionDialog.show();

                break;
            case R.id.voucherButton:
                // Added by Anh Tuan
                if (NetworkAvailability.chkStatus(getActivity())) {
                    intent = new Intent(getActivity(), ExtraActivity.class);
                    intent.putExtra("url", "http://tools.danet.vn/account/voucher.php?id=" + WhiteLabelApplication.getInstance().getAccessToken());
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
                }

                break;
            case R.id.activateTVButton:
                intent = new Intent(getActivity(), BarcodeCaptureActivity.class);
                startActivity(intent);
                break;
            case R.id.signOutButton:
                WhiteLabelApplication whiteLabelApplication = WhiteLabelApplication.getInstance();
                whiteLabelApplication.setIsUserLoggedIn(false);
                whiteLabelApplication.setAccessToken(null);
                whiteLabelApplication.setUser(null);

                PreferenceHelper.setDataInSharedPreference(getActivity(), getActivity().getResources().getString(R.string.user_info), null);
                PreferenceHelper.setDataInSharedPreference(getActivity(), getActivity().getResources().getString(R.string.user_is_logged_in), false);

                new Handler(Looper.myLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Fragment fragment = HomePageFragment.newInstance(WhiteLabelApplication.getInstance().getLicenseType());
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.frame_layout_main, fragment)
                                .commit();
                        ((MainActivity) getActivity()).onReloadMenuHandler();
                    }
                });
                break;
            case R.id.privacyPolicyTextView:
                if (NetworkAvailability.chkStatus(getActivity())) {
                    intent = new Intent(getActivity(), ContentActivity.class);
                    intent.putExtra("url", PRIVACY_POLICY_SITE_URL);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.termTextView:
                if (NetworkAvailability.chkStatus(getActivity())) {
                    intent = new Intent(getActivity(), ContentActivity.class);
                    intent.putExtra("url", TERMS_SITE_URL);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.helpTextView:
                if (NetworkAvailability.chkStatus(getActivity())) {
                    intent = new Intent(getActivity(), ContentActivity.class);
                    intent.putExtra("url", HELP_SITE_URL);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
        clickedId = v.getId();
    }

    @Override
    public void onPositiveButtonClick(DialogInterface dialog) {
        Intent intent = null;

        switch (clickedId) {
            case R.id.renewBuffetButton:
                intent = new Intent(getActivity(), SVODActivity.class);
                intent.putExtra("type","1");
                //startActivity(intent);
                break;
            case R.id.cineplexButton:
                //Nạp điểm bằng thẻ cào
                //https://payment.danet.vn/prepaidcard/?identifier=anhtuan@bhdvn.com&accessToken=8d969d116214598aa29c2b1fbb7ee8df
                intent = new Intent(getActivity(), ExtraActivity.class);
                intent.putExtra("url", "https://payment.danet.vn/prepaidcard/?identifier=" + user.getIdentifier() + "&accessToken=" + WhiteLabelApplication.getInstance().getAccessToken());
                break;
            case R.id.privacyPolicyTextView:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_SITE_URL));
                break;
            case R.id.termTextView:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(TERMS_SITE_URL));
                break;
            case R.id.helpTextView:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(HELP_SITE_URL));
                break;
            case R.id.createAccountTextView:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(CREATE_ACCOUNT_SITE_URL));
                break;
        }
        if (intent != null) {
            startActivity(intent);
            dialog.dismiss();
        }
    }

    @Override
    public void onNegativeButtonClick(DialogInterface dialog) {
        Intent intent;
        switch (clickedId) {
            case R.id.renewBuffetButton :
                if(!checkUserSubscribed(user)){
                    intent = new Intent(getActivity(), SVODActivity.class);
                    intent.putExtra("type", "2");
                    startActivity(intent);
                }
                break;
            case R.id.cineplexButton :
                if (NetworkAvailability.chkStatus(getActivity())) {
                    intent = new Intent(getActivity(), TVODActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }

    }

    private boolean checkUserSubscribed(User user) {
        if (user == null || user.getSubscription() == null)
            return false;

        Date currentDate = new Date();

        if (currentDate.before(user.getSubscription().getEndDate()))
            return true;
        else
            return false;
    }


    /*creating auth token after that fetch user information*/
    private void userAuthenticate(String provider, String identifier, String password) {
        String[] params = {provider, identifier, password};
        AuthenticationParser authenticationParser = new AuthenticationParser(getActivity(), new AuthenticationUserResultReceived() {
            @Override
            public void onResult(String error, User result) {
                if (error == null && result != null) {
                    UserDetailUpdateParser userDetailUpdateParser = new UserDetailUpdateParser();
                    ViewUtils.execute(userDetailUpdateParser, result.getAccessToken());
                } else {
                    if (error != null && error.equals(getActivity().getResources().getString(R.string.un_authorised_exception_code)))
                        Toast.makeText(getActivity(), getResources().getString(R.string.un_authorised_user), Toast.LENGTH_LONG).show();
                    else if (error != null && error.equals(getActivity().getResources().getString(R.string.not_found_exception_code)))
                        Toast.makeText(getActivity(), getResources().getString(R.string.user_not_exits), Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                }
            }
        });
        ViewUtils.execute(authenticationParser, params);
    }

    /*User details update parser*/
    public class UserDetailUpdateParser extends AsyncTask<Object, String, User> {

        private String error = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressView.show();
        }

        @Override
        protected User doInBackground(Object... params) {
            ContentHandler contentHandler = ContentHandler.getInstance();
            User user = null;
            try {
                user = contentHandler.getUser((String) params[0]);
                user.setAccessToken((String) params[0]);
            } catch (Exception e) {
                error = e.getMessage();
            }
            return user;
        }

        @Override
        protected void onPostExecute(User userResults) {
            super.onPostExecute(userResults);
            if (progressView!=null)
                progressView.dismiss();
            if (error == null && userResults != null) {
                //Log.d(TAG,userResults.toString());
                if(PreferenceHelper.getSharedPrefData(getActivity(), getActivity().getResources().getString(R.string.user_info), User.class)!= null) {
                    if (userResults.getIdentifier() == null) {
                        userResults.setIdentifier(PreferenceHelper.getSharedPrefData(getActivity(), getActivity().getResources().getString(R.string.user_info), User.class).getIdentifier());
                    }
                    if (userResults.getPassword() == null) {
                        userResults.setPassword(PreferenceHelper.getSharedPrefData(getActivity(), getActivity().getResources().getString(R.string.user_info), User.class).getPassword());
                    }
                }
                PreferenceHelper.setDataInSharedPreference(getActivity(), getActivity().getResources().getString(R.string.user_info), userResults);
                user = userResults;
                     /*set all widgets/view values*/
                setWidgetsValues();
            } else {
                if (error != null && error.equals(getActivity().getResources().getString(R.string.un_authorised_exception_code))) {
                    User userInfo = PreferenceHelper.getSharedPrefData(getActivity(), getActivity().getResources().getString(R.string.user_info), User.class);
                    userAuthenticate(userInfo.getProvider(), userInfo.getIdentifier(), userInfo.getPassword());
                } else
                    Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
            }
        }
    }
}
