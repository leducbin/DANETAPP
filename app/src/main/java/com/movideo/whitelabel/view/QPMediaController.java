package com.movideo.whitelabel.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import com.movideo.whitelabel.R;
import com.movideo.whitelabel.widget.LVMediaController;

public class QPMediaController extends LVMediaController{

	private final static String TAG = "QPMediaController";
	//private TextView mediaNameUIText;
	//private TextView batteryUIText;
	//private TextView hwCodecIndicText;
	private ImageButton ccButton;

	private String mediaName = null;

	//private String batteryText = null;


	public QPMediaController(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public QPMediaController(Context context) {
		super(context);
	}

	@Override
	protected void initAdditionalControls(Context context) {
		//mediaNameUIText = (TextView)findViewById(R.id.mediaNameText);
		//batteryUIText = (TextView)findViewById(R.id.battery_level);
		//hwCodecIndicText = (TextView)findViewById(R.id.hw_codec_indic);
		ccButton = (ImageButton) findViewById(R.id.lvwidget_mediacontroller_subtitle_btn);

		super.initAdditionalControls(context);

	}

	public ImageButton getSubtitleButton (){
		return ccButton;
	}

//	public void setMediaName(String name) {
//		this.mediaName = name;
//		if(null != mediaNameUIText) {
//			mediaNameUIText.setText(mediaName);
//		}
//	}

//	public void setBatteryText(String text) {
//		batteryText = text;
//		if(null != batteryUIText) {
//			batteryUIText.setText(batteryText);
//		}
//	}

//	public void setUseHWCodec(boolean useHw) {
//		if(useHw) {
//			hwCodecIndicText.setText("HW");
//		}
//		else {
//			hwCodecIndicText.setText("SW");
//		}
//	}

}
