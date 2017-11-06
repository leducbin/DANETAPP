package com.movideo.whitelabel;

/**
 * Created by BHD on 9/15/2017.
 */

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.movideo.baracus.model.user.User;
import com.movideo.whitelabel.util.DialogEventListeners;
import com.movideo.whitelabel.util.Utils;
import com.movideo.whitelabel.util.ViewUtils;
import com.movideo.whitelabel.view.AddMessageDialogView;
import com.movideo.whitelabel.view.AddTwoButtonDialogView;
import com.movideo.whitelabel.view.ProgressView;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

/**
 * Activity for the multi-tracker app.  This app detects barcodes and displays the value with the
 * rear facing camera. During detection overlay graphics are drawn to indicate the position,
 * size, and ID of each barcode.
 */
public final class BarcodeCaptureActivity extends AppCompatActivity {
    private static final String TAG = "Barcode-reader";

    // intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    private Boolean justCreated = true;
    private SurfaceView cameraView;
    private CameraSource cameraSource;
    private Button activateButton;

    private ProgressView progressView;
    private User user;
    private AddTwoButtonDialogView dialogView;
    private AddMessageDialogView messageDialogView;
    private DialogEventListeners errorDialogListener;

    private boolean dialogLock = true;
    private boolean messageDialogLock = true;
    /**
     * Initializes the UI and creates the detector pipeline.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.barcode_capture);
        if (ViewUtils.isTablet(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        progressView = new ProgressView(this);
        cameraView = (SurfaceView)findViewById(R.id.camera_view);
        activateButton = (Button) findViewById(R.id.activateButton);
        setUpListeners();
       // progressView = new ProgressView(this);

        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }
    }

    private void setUpListeners(){
        errorDialogListener = new DialogEventListeners() {
            @Override
            public void onPositiveButtonClick(DialogInterface dialog) {
                try {
                    dismissDialog();
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException ie) {
                    Log.e("CAMERA SOURCE", ie.getMessage());
                } catch (SecurityException se){
                    Log.e("CAMERA SOURCE", se.getMessage());
                }
            }

            @Override
            public void onNegativeButtonClick(DialogInterface dialog) {

                dismissDialog();
                finish();
            }
        };
        activateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = BarcodeCaptureActivity.this;
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                LayoutInflater inflater = activity.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.dialog_with_edit_text, null);
                dialogBuilder.setView(dialogView);

                final EditText edt = (EditText) dialogView.findViewById(R.id.edit1);

                dialogBuilder.setTitle("Nhập mã đăng nhập");
                dialogBuilder.setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //do something with edt.getText().toString()
                        SyncAccountTask syncAccountTask = new SyncAccountTask();
                        Utils.executeInMultiThread(syncAccountTask, edt.getText().toString());

                    }
                });
                dialogBuilder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            cameraSource.start(cameraView.getHolder());
                        } catch (IOException ie){
                            Log.d(TAG, ie.getMessage());
                        } catch (SecurityException se){
                            Log.d(TAG, se.getMessage());
                        }
                        //pass
                    }
                });
                AlertDialog b = dialogBuilder.create();
                cameraSource.stop();
                b.show();
            }

        });
    }
    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        findViewById(R.id.topLayout).setOnClickListener(listener);

        Snackbar.make(cameraView, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     *
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private void createCameraSource() {
        Context context = getApplicationContext();

        // A barcode detector is created to track barcodes.  An associated multi-processor instance
        // is set to receive the barcode detection results, track the barcodes, and maintain
        // graphics for each barcode on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each barcode.
        final BarcodeDetector barcodeDetector =
                new BarcodeDetector.Builder(context)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        if (!barcodeDetector.isOperational()) {
            Log.w(TAG, "Detector dependencies are not yet available.");
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1600, 1024)
                .setAutoFocusEnabled(true)
                .build();


        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(final SurfaceHolder holder) {
                new AsyncTask<Void, Void, Boolean>() {
                    protected Boolean doInBackground(Void... params) {
                        try {
                            cameraSource.start(holder);
                        } catch (IOException ie) {
                            Log.e("CAMERA SOURCE", ie.getMessage());
                        } catch (SecurityException se){
                            Log.e("CAMERA SOURCE", se.getMessage());
                        }
                        return null;
                    }
                    protected void onPostExecute(Boolean result) {
                        justCreated = false;
                    }
                }.execute();



            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if(cameraSource != null)
                    cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {
                    final Handler myHandler= new Handler(Looper.getMainLooper());
                    myHandler.post(new Runnable() {
                        public void run() {
                            cameraSource.stop();
                            Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                            v.vibrate(500);
                            SyncAccountTask syncAccountTask = new SyncAccountTask();
                            Utils.executeInMultiThread(syncAccountTask, barcodes.valueAt(0).displayValue);
                            barcodes.clear();

                        }

                    });
                }
            }
        });
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if(!justCreated)
            startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if(cameraSource != null)
            cameraSource.stop();
//        if (mPreview != null) {
//            mPreview.stop();
//        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cameraSource != null)
            cameraSource.release();
//        if (mPreview != null) {
//            mPreview.release();
//        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Multitracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    public void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }
        try {
            cameraSource.start(cameraView.getHolder());
        } catch (IOException ie) {
            Log.e("CAMERA SOURCE", ie.getMessage());
        } catch (SecurityException se){
            Log.e("CAMERA SOURCE", se.getMessage());
        }
    }

    public void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private class SyncAccountTask extends AsyncTask<String, Void,String > {

        private URL url;
        private HttpURLConnection conn;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(progressView != null)
                progressView.show();
            user = WhiteLabelApplication.getInstance().getUser();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                url = new URL( "http://tools.danet.vn/account/activate/?op=update");
                conn = (HttpURLConnection) url.openConnection();
                String otp = params[0];
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("otp", otp);
                jsonObject.put("access_token", user.getAccessToken());
                jsonObject.put("identifier", user.getIdentifier());
                jsonObject.put("family_name", user.getFamilyName());
                jsonObject.put("given_name", user.getGivenName());
                jsonObject.put("phone", user.getPhone());
                jsonObject.put("date_of_birth", user.getDateOfBirth());
                jsonObject.put("credits", user.getCredits().toString());
                String subscribe_expire_date;
                if(user.getSubscription() != null){
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    subscribe_expire_date = dateFormat.format(user.getSubscription().getEndDate());
                } else {
                    subscribe_expire_date = "1970-01-01T17:06:39";
                }

                jsonObject.put("subscribe_expire_date", subscribe_expire_date);
                OutputStream output = new BufferedOutputStream(conn.getOutputStream());
                output.write(jsonObject.toString().getBytes());
                output.flush();
                output.close();
                conn.connect();
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
                return sb.toString();
            }
            catch(Exception e){
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
            finally{
                conn.disconnect();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String inputStream) {
            super.onPostExecute(inputStream);
            if(progressView != null)
                progressView.dismiss();
            try {
                JSONObject mainObject = new JSONObject(inputStream);
                if(mainObject.has("status") && mainObject.get("status").equals("success")){
                    messageDialogView = new AddMessageDialogView(BarcodeCaptureActivity.this, "Đăng nhập thành công", "Tài khoản của bạn đã được đăng nhập thành công trên ứng dụng DANET của SmartTV.\nVui lòng đợi trong giây lát", "Ok", new DialogEventListeners() {
                        @Override
                        public void onPositiveButtonClick(DialogInterface dialog) {
                            dismissMessageDialog();
                            finish();
                        }

                        @Override
                        public void onNegativeButtonClick(DialogInterface dialog) {

                        }
                    });
                    showMessageDialogView();

                } else{
                    dialogView = new AddTwoButtonDialogView(BarcodeCaptureActivity.this, "Sai mã đăng nhập", "Mã đăng nhập không hợp lệ. Vui lòng thử lại", "Thử lại", "Thoát", errorDialogListener);

                    showDialogView();
                }

            }
            catch (Exception e){
                dialogView = new AddTwoButtonDialogView(BarcodeCaptureActivity.this, "Lỗi hệ thống", "Không lấy được thông tin đang nhập", "Thử lại", "Thoát", errorDialogListener);
                showDialogView();
                e.printStackTrace();
            }
        }

    }

    private void showDialogView(){
        if(dialogView!=null && dialogLock) {
            dialogView.show();
            dialogLock = false;
        }

    }

    private void dismissDialog(){
        if(dialogView!=null) {
            dialogView.dismiss();
            dialogLock = true;
        }
    }

    private void showMessageDialogView(){
        if(messageDialogView!=null && messageDialogLock) {
            messageDialogView.show();
            messageDialogLock = false;
        }

    }

    private void dismissMessageDialog(){
        if(messageDialogView!=null) {
            messageDialogView.dismiss();
            messageDialogLock = true;
        }
    }
}

