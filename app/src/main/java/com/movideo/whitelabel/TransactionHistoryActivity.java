package com.movideo.whitelabel;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.movideo.baracus.model.VOD.Credit;
import com.movideo.baracus.model.user.Order;
import com.movideo.whitelabel.communication.ContentRequestListener;
import com.movideo.whitelabel.communication.GetOrderTask;
import com.movideo.whitelabel.util.Utils;
import com.movideo.whitelabel.util.ViewUtils;
import com.movideo.whitelabel.view.AddTwoButtonDialogView;
import com.movideo.whitelabel.widgetutils.WidgetsUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by ThanhTam on 1/6/2017.
 */

public class TransactionHistoryActivity  extends AppCompatActivity implements ContentRequestListener<List<Order>> {
    private static final String TAG = TransactionHistoryActivity.class.getSimpleName();
    private String accessToken ;
    private String guid;
    private LinearLayout listTransactionHistory;
    private List<Credit> credits = new ArrayList<>();
    private AddTwoButtonDialogView dialogView;
    private View column_separatedLine;
    private View row_separatedLine;
    private SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
    @Bind(R.id.progressBarTransaction)
    ProgressBar progressBarTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);
        if (ViewUtils.isTablet(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        listTransactionHistory = (LinearLayout) findViewById(R.id.ListTransactionHistory);
        listTransactionHistory.setOrientation(LinearLayout.VERTICAL);
        accessToken = WhiteLabelApplication.getInstance().getAccessToken();
        ButterKnife.bind(this);
        progressBarTransaction.setVisibility(View.VISIBLE);
        loadTransactionHistory();

    }

    private void loadTransactionHistory(){
        GetOrderTask getOrderTask = new GetOrderTask(this);
        WidgetsUtils.Log.d(TAG,"start load credits");
        Utils.executeInMultiThread(getOrderTask,accessToken);
    }

    @Override
    public void onRequestCompleted(List<Order> orders) {
        if(progressBarTransaction.isShown()){
            progressBarTransaction.setVisibility(View.GONE);
        }
        if(orders.isEmpty()){
            LinearLayout row = new LinearLayout(TransactionHistoryActivity.this);
            row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins( (int) (getResources().getDimension(R.dimen.cineplex_credit_button_margin)),  //marginLeft
                    (int) (getResources().getDimension(R.dimen.cineplex_credit_button_margin)),             //marginTop
                    (int) (getResources().getDimension(R.dimen.cineplex_credit_button_margin)),             //marginRight
                    0);                       //marginBottom
            row.setLayoutParams(params);
            row.setGravity(Gravity.CENTER);
            TextView emptyOrder = new TextView((TransactionHistoryActivity.this));
            emptyOrder.setTextColor(Color.WHITE);
            emptyOrder.setText("Không có lịch sử giao dịch");
            //emptyOrder.setGravity(Gravity.CENTER);
            emptyOrder.setTextSize(18);
            row.addView(emptyOrder);
            listTransactionHistory.addView(row);

        }
        else {
            Log.d(TAG,orders.toString());

            for (Order order : orders) {
                createHistoryTable(order);
            }
        }
    }

    @Override
    public void onRequestFail(Throwable throwable) {
        if(progressBarTransaction.isShown()){
            progressBarTransaction.setVisibility(View.GONE);
        }
        Toast.makeText(TransactionHistoryActivity.this,"Fail to get credits",Toast.LENGTH_LONG).show();
        Log.e(TAG, throwable.getMessage(), throwable);
    }

    public void createHistoryTable(Order order){
        LinearLayout row = new LinearLayout(TransactionHistoryActivity.this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins( 15,  //marginLeft
                (int) (getResources().getDimension(R.dimen.cineplex_credit_button_margin)),             //marginTop
                15,             //marginRight
                (int) (getResources().getDimension(R.dimen.cineplex_credit_button_margin)));                       //marginBottom
        row.setLayoutParams(params);

        LinearLayout firstColumn = new LinearLayout(this);
        firstColumn.setOrientation(LinearLayout.VERTICAL);
        firstColumn.setLayoutParams(new LinearLayout.LayoutParams(0,LayoutParams.MATCH_PARENT,0.25f));
        //Order Id
        TextView mId = new TextView(this);
        mId.setText(order.getId().toString());
        mId.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,0,0.5f));
        //Order Date
        TextView mDate = new TextView(TransactionHistoryActivity.this);
        mDate.setText(format.format(order.getOrderDate()));
        mDate.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,0,0.5f));

        row_separatedLine = new View(TransactionHistoryActivity.this);
        row_separatedLine.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 3));
        row_separatedLine.setBackgroundColor(Color.parseColor("#40FFFFFF"));

        firstColumn.addView(mId);
        firstColumn.addView(row_separatedLine);
        firstColumn.addView(mDate);

        column_separatedLine = new View(TransactionHistoryActivity.this);
        column_separatedLine.setLayoutParams(new LinearLayout.LayoutParams(3, LayoutParams.MATCH_PARENT));
        column_separatedLine.setBackgroundColor(Color.parseColor("#40FFFFFF"));
        //Order Description
        TextView mHistory = new TextView(TransactionHistoryActivity.this);
        mHistory.setLayoutParams(new LinearLayout.LayoutParams(0,LayoutParams.MATCH_PARENT,0.55f));
        mHistory.setText(order.getDescription());
        mHistory.setId(order.getId());

        row.addView(firstColumn);
        row.addView(column_separatedLine);
        row.addView(mHistory);
        //Order Amount
        TextView mAmount = new TextView(TransactionHistoryActivity.this);
        mAmount.setText(order.getAmount());
        mAmount.setLayoutParams(new LinearLayout.LayoutParams(0,LayoutParams.MATCH_PARENT,0.2f));

        column_separatedLine = new View(TransactionHistoryActivity.this);
        column_separatedLine.setLayoutParams(new LinearLayout.LayoutParams(3, LayoutParams.MATCH_PARENT));
        column_separatedLine.setBackgroundColor(Color.parseColor("#40FFFFFF"));
        row.addView(column_separatedLine);
        row.addView(mAmount);

        row_separatedLine = new View(TransactionHistoryActivity.this);
        row_separatedLine.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 3));
        row_separatedLine.setBackgroundColor(Color.parseColor("#40FFFFFF"));

        setTextViewStyle(Arrays.asList( mId , mDate , mAmount , mHistory));
        listTransactionHistory.addView(row);
        listTransactionHistory.addView(row_separatedLine);

    }

    private void setTextViewStyle(List<TextView> textViews){
        for(TextView textView : textViews) {
            textView.setTextColor(Color.WHITE);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(12);
        }
    }

}
