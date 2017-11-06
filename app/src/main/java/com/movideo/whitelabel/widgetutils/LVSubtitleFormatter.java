package com.movideo.whitelabel.widgetutils;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

import com.lifevibes.LVSurfaceView;
import com.lifevibes.lvmediaplayer.LVSubtitle;
import com.lifevibes.lvmediaplayer.LVSubtitleCharAttributes;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for subtitle formatting.<br>
 * The purpose of this class is to provide a method to process Subtile attributes<br>
 * like (foreground/background colors, transparency, font attributes,...) into<br>
 * a SpannableString directly usable in a Android TextView<br>
 */
public class LVSubtitleFormatter {

	private static final String TAG  =  "LVSubtitleFormatter"; // Tag used for Android logging

	/** Nested class that groups all Foreground formattings that can be applied */
	private static final class ForegroundSpans {
		/**
		 * Create a ForegroundSpan with the RGB888 color and the display mode flag.
		 * @param aForDisplayMode is display mode of the text, possible values are DisplayMode_Caption_Off, DisplayMode_Caption_On, DisplayMode_Caption_On_Flash (cf: LVSubtitle class)
		 * @param aForColor is the color to be applied encoded in RGB888. Red, Green Blue, each coded on 8 bits. 24 less significant bits
		 */
		public static final ForegroundColorSpan getRGBColorSpan(byte aForDisplayMode, int aForColor) {

			int colorRedValue   = (aForColor & 0x00FF0000) >> 16; /** Mask RED color and move it in 8 bits */
			int colorGreenValue = (aForColor & 0x0000FF00) >> 8;  /** Mask GREEN color and move it in 8 bits */
			int colorBlueValue  = (aForColor & 0x000000FF) >> 0;  /** Mask BLUE color and move it in 8 bits */

			int alphaValue = 0;

			switch( aForDisplayMode ) {
			case LVSubtitleCharAttributes.DisplayMode_Caption_Off:
				/** Alpha = 0 means fully transparent */
				alphaValue = 0;
				break;
			case LVSubtitleCharAttributes.DisplayMode_Caption_On:
				/** Alpha = 255 means full opaque */
				alphaValue = 255;
				break;
			default:  /** For LVSubtitleCharAttributes.DisplayMode_Caption_On_Flash -- SHOULD be handled in a different way - Only for test purpose*/
				alphaValue = 128;
				break;
			}
			//DEBUG Log.d(TAG, "ForegroundColorSpan getRGBColorSpan A=" + alphaValue + " R=" + colorRedValue +  " G=" + colorGreenValue + " B=" + colorBlueValue);
			return new ForegroundColorSpan(Color.argb(alphaValue, colorRedValue, colorGreenValue, colorBlueValue));
		}

		/**
		 * Create a StyleSpan with the Foreground Font Style value from the subtitle.<br>
		 * It only contains Span for Default, Italic and/or Bold attributes.
		 * @param aForFontStyle is the font style of the text, possible values can be (cumulated possible):<br>
		 * FontStyle_None, FontStyle_Underline, FontStyle_Italic, FontStyle_Bold, FontStyle_Strikethrough (cf: LVSubtitle class)
		 */
		public static final StyleSpan getFontStyleSpan(byte aForFontStyle) {

			int styleValue = android.graphics.Typeface.NORMAL;

			if((aForFontStyle & LVSubtitleCharAttributes.FontStyle_Italic) == LVSubtitleCharAttributes.FontStyle_Italic) styleValue += android.graphics.Typeface.ITALIC;
			if((aForFontStyle & LVSubtitleCharAttributes.FontStyle_Bold)   == LVSubtitleCharAttributes.FontStyle_Bold)   styleValue += android.graphics.Typeface.BOLD;

			//Log.d(TAG, "StyleSpan getFontStyleSpan Style=" + styleValue + " for aForFontStyle=" + aForFontStyle);

			return new StyleSpan(styleValue);
		}

		/**
		 * Create a StrikethroughSpan with the Foreground Font Style value from the subtitle.<br>
		 * It only contains Span for Strikethrough attribute.
		 * @param aForFontStyle is the font style of the text, possible values can be (cumulated possible):<br>
		 * FontStyle_None, FontStyle_Underline, FontStyle_Italic, FontStyle_Bold, FontStyle_Strikethrough (cf: LVSubtitle class)
		 */
		public static final StrikethroughSpan getFontStrikethroughSpan(byte aForFontStyle) {

			StrikethroughSpan retValue = null;

			if((aForFontStyle & LVSubtitleCharAttributes.FontStyle_Strikethrough) == LVSubtitleCharAttributes.FontStyle_Strikethrough)
				retValue = new StrikethroughSpan();
			//if(retValue != null)
			//Log.d(TAG, "StrikethroughSpan getFontStrikethroughSpan Strikethrough for aForFontStyle=" + aForFontStyle);
			//else
			//Log.d(TAG, "StrikethroughSpan getFontStrikethroughSpan NO Strikethrough for aForFontStyle=" + aForFontStyle);

			return retValue;

		}

		/**
		 * Create a UnderlineSpan with the Foreground Font Style value from the subtitle.<br>
		 * It only contains Span for Underline attribute.
		 * @param aForFontStyle is the font style of the text, possible values can be (cumulated possible):<br>
		 * FontStyle_None, FontStyle_Underline, FontStyle_Italic, FontStyle_Bold, FontStyle_Strikethrough (cf: LVSubtitle class)
		 */
		public static final UnderlineSpan getFontUnderlineSpan(byte aForFontStyle) {

			UnderlineSpan retValue = null;

			if((aForFontStyle & LVSubtitleCharAttributes.FontStyle_Underline) == LVSubtitleCharAttributes.FontStyle_Underline)
				retValue = new UnderlineSpan();

			//if(retValue != null)
			//Log.d(TAG, "UnderlineSpan getFontUnderlineSpan Underline for aForFontStyle=" + aForFontStyle);
			//else
			//Log.d(TAG, "UnderlineSpan getFontUnderlineSpan NO Underline for aForFontStyle=" + aForFontStyle);

			return retValue;
		}
	}

	/** Nested class that groups all Background formattings that can be applied */
	private static final class BackgroundSpans {

		/**
		 * Create a Background Span with the RGB888 color and the display mode flag.
		 * @param aForDisplayMode is display mode of the text, possible values are DisplayMode_Caption_Off, DisplayMode_Caption_On (cf: LVSubtitle class)
		 * @param aForColor is the color to be applied encoded in RGB888. Red, Green Blue, each coded on 8 bits. 24 less significant bits
		 */
		public static final BackgroundColorSpan getRGBColorSpan(byte aBackDisplayMode,int aBackColor, char aBackTransparencyLevel) {

			int colorRedValue   = (aBackColor & 0x00FF0000) >> 16;
			int colorGreenValue = (aBackColor & 0x0000FF00) >> 8;
			int colorBlueValue  = (aBackColor & 0x000000FF) >> 0;

			int alphaValue = 0;

			//DEBUG Log.d(TAG, "BackgroundColorSpan getRGBColorSpan aBackDisplayMode=" + aBackDisplayMode + " aBackTransparencyLevel=" + (short)aBackTransparencyLevel);
			if((aBackDisplayMode & LVSubtitleCharAttributes.DisplayMode_Caption_On) == LVSubtitleCharAttributes.DisplayMode_Caption_On)
			{
				/** Alhpa = 0 means fully transparent , 255 full opaque */
				if(255 < aBackTransparencyLevel) aBackTransparencyLevel = 255;
					alphaValue = 255 - aBackTransparencyLevel;
			}else{   /** Manage Mode_Caption_On_Flash like DisplayMode_Caption_Off as it is not supported for background */
				alphaValue = 0;
			}

			//DEBUG Log.d(TAG, "BackgroundColorSpan getRGBColorSpan A=" + alphaValue + " R=" + colorRedValue +  " G=" + colorGreenValue + " B=" + colorBlueValue + " aBackDisplayMode=" + aBackDisplayMode + " aBackTransparencyLevel=" + (short)aBackTransparencyLevel);
			return new BackgroundColorSpan(Color.argb(alphaValue, colorRedValue, colorGreenValue, colorBlueValue));
		}
	}

	/** Nested class that groups all SMPTE parameters that can be applied */
	private static class ParamsSMPTE{
		int mTop = 0;
		int mLeft = 0;
		int mWidth = 0;
		int mHeight = 0;
	}

	/**
	 * This is the entry function of Subtitle Formatter class.<br>
	 * It is a static class as it is not necessary to create an object of this class to use this function.<br>
	 * The purpose of this function is to create "User Interface" formatting usable by Android TextView container.<br>
	 * Typically it creates and apply different "Span" and apply them character by character to a "SpannableString". <br>
	 * This class is responsible of managing foreground flash mode, that is why this class has the LVSubtitle and the TextView<br>
	 * as inputs and does not directly return a SpannableString.
	 * This class applies a spefic formatting for following subtitles:
	 * 		- CEA608 subtitles
	 * 		- Harmonic SMPTE subtitles (https://tools.ietf.org/html/draft-smpte-id3-http-live-streaming-00).
	 * @param surface is the surface where the video is played (it is used to compute the display dimensions).
	 * @param sub is the Subtitle to process to get at first the text and then (if applicable the character attributes array to apply).
	 * @param tv is the Android TextView container that will display the Subtitle.
	 */
	public static void formatSubtitleAttribute(LVSurfaceView surface, LVSubtitle sub,TextView tv)
	{
		if (null != sub) {
			String text = sub.getTextPlanned();
			if(null != text) {
				Log.d(TAG, "formatSubtitleAttribute ORIG Source=" + sub.getType());

				if(LVSubtitle.Type_CC_CEA608 == sub.getType()) { /** Apply formatting for CEA608 Closed Caption*/
					formatTextAsCEA608(text, sub.getCharArrayAttributes(), tv);
				}else if (LVSubtitle.Type_SMPTE == sub.getType()){ /** Apply formatting for SMPTE subtitle*/
					formatTextAsHarmonicSMPTE(surface, sub, tv);
				}else { /** We are in common subtitle case (subtitle file) LVSubtitle.Source_File or Unknown ? */
					/** Set default subtitle style and the text with no formatting*/
					tv.setTypeface(Typeface.DEFAULT_BOLD);
					tv.setTextScaleX(1.0f);
					tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f);
					tv.setShadowLayer(3.0f /** Radius */,5.0f /** Dx */,5.0f /** Dy */,0xFF000000 /** Black Color with alpha = 255*/);
					tv.setText(text);
				}
			}else {
				/** If the text is null update the text view anyway in order to remove last subtitle */
				tv.setText(null);
			}
		}
	}


	private static void formatTextAsCEA608(String text, LVSubtitleCharAttributes[] charAttributes, TextView textView) {
		SpannableString ss = new SpannableString(text);
		int idx = 0;
		int attrIdx = 0;
		for(idx = 0 ; idx < text.length() ; idx++) {
			//DEBUG Log.d(TAG, "formatSubtitleAttribute idx=" + idx + " C=" + text.charAt(idx) + " isWhitespace=" + Character.isWhitespace(text.charAt(idx)) + " getType=" + (Character.getType(text.charAt(idx))));
			if(     ! Character.isWhitespace(text.charAt(idx))  /** check it is not a new line character */
					|| !(Character.getType(text.charAt(idx)) == Character.CONTROL) ) /** control not usual whitespace */
			{
				/** Test  subCharAttr not null*/
				if(null != charAttributes) {
					/** Test  subCharAttr.length*/
					if(attrIdx < charAttributes.length) {
						// DEBUG Log.d(TAG, "formatSubtitleAttribute idx=" + idx + " C=" + text.charAt(idx) + " isWhitespace=" + Character.isWhitespace(text.charAt(idx)) + " getType=" + (Character.getType(text.charAt(idx))));

						ss.setSpan(ForegroundSpans.getRGBColorSpan(charAttributes[attrIdx].getForegroundDisplayMode(),
								charAttributes[attrIdx].getForegroundColor())
								,idx, idx+1, Spannable.SPAN_COMPOSING);

						ss.setSpan(ForegroundSpans.getFontStyleSpan(charAttributes[attrIdx].getForegroundFontStyle())
								,idx, idx+1, Spannable.SPAN_COMPOSING);

						ss.setSpan(ForegroundSpans.getFontStrikethroughSpan(charAttributes[attrIdx].getForegroundFontStyle())
								,idx, idx+1, Spannable.SPAN_COMPOSING);

						ss.setSpan(ForegroundSpans.getFontUnderlineSpan(charAttributes[attrIdx].getForegroundFontStyle())
								,idx, idx+1, Spannable.SPAN_COMPOSING);

						ss.setSpan(BackgroundSpans.getRGBColorSpan(charAttributes[attrIdx].getBackgoundDisplayMode(),
								charAttributes[attrIdx].getBackgoundColor(),
								charAttributes[attrIdx].getBackgoundTransparencyLevel())
								,idx, idx+1, Spannable.SPAN_COMPOSING);
						attrIdx++;
					}
				}
			}
		}
		textView.setTypeface(Typeface.MONOSPACE);
		textView.setTextScaleX(1.3f);
		textView.setShadowLayer(0,0,0,0);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18.0f);
		textView.setText(ss);
	}


	private static void formatTextAsHarmonicSMPTE(LVSurfaceView surface, LVSubtitle sub,TextView tv)  {
		if (null != sub) {
			try{
				/**  Parse the position information and the encoded PNG from LVSubtitle.
				 *  The SMPTE subtitle returned by the SDK has the following format:
				 *  <div begin="00:00:01:229" end="00:00:02:152" tts:extent="39% 6%"tts:origin="30% 87%">
				 *  <metadata>
				 *      <smpte:image imagetype="PNG" encoding="Base64">
				 *              iVBO
				 *              ...
				 *              gg==
				 *          </smpte:image>
				 *      </metadata>
				 *  </div>
				 */
				StringBuffer htmlContent = new StringBuffer("<html><body>");
				String pngString = sub.getText().substring(sub.getText().indexOf("ase64\">") + 7).split("<")[0];
				htmlContent.append("<img src=\"data:image/png;base64,"+ pngString + "\"/>");
				String html = htmlContent.toString();

				if(null != tv && null != surface){
					/** Get harmmonic SMPTE subtitle positions */
					ParamsSMPTE smpteParams = getHarmonicSMPTEParams(surface.getWidth(), surface.getHeight(), sub);
					PngImageParser pngparser = new PngImageParser(smpteParams);
					/** Set PNG to text view */
					tv.destroyDrawingCache();
					tv.clearComposingText();
					tv.setText(Html.fromHtml(html, pngparser, null));
				}
			}catch (Exception e){
				Log.e(TAG, "formatTextAsHarmonicSMPTE exception: " + e.getMessage());
			}
		}
	}

	public static class PngImageParser implements ImageGetter {

		private ParamsSMPTE mSmpteParams = null;
		PngImageParser(ParamsSMPTE smpteParams){
			mSmpteParams = smpteParams;
		}

		/** Get the png for harmonic SMPTE subtitles */
		public Drawable getDrawable(String source) {
			Drawable drawable = null;
			byte[] arr = null;
			try{
				String substring = source.substring(source.indexOf("base64,")+ 7);
				if (null != substring){
					/** decode the base64 png */
					arr = Base64.decode(substring.getBytes(), Base64.DEFAULT);
					InputStream imageStream = new BufferedInputStream(new ByteArrayInputStream(arr));
					drawable = Drawable.createFromStream(imageStream, null);
					/** For SMPTE subtitles the textview is positioned at the top-left corner, so we compute the bounds from this position */
					drawable.setBounds(mSmpteParams.mLeft, mSmpteParams.mTop, mSmpteParams.mLeft + mSmpteParams.mWidth, mSmpteParams.mTop + mSmpteParams.mHeight);
				}
			}catch (Exception e){
				Log.e(TAG, "PngImageParser:getDrawable exception: " + e.getMessage());
			}
			return drawable;
		}
	}

	private static ParamsSMPTE getHarmonicSMPTEParams(int surfaceWidth, int surfaceHeight, LVSubtitle sub){
		ParamsSMPTE params = new ParamsSMPTE();
		Pattern divPattern = Pattern.compile("<div begin=\".+?\" end=\".+?\" tts:extent=\"([0-9]+)% ([0-9]+)%\" tts:origin=\"([0-9]+)% ([0-9]+)%\">(.*?)</div>", Pattern.DOTALL);
		Matcher divMatch = divPattern.matcher(sub.getText());
		if(divMatch.find()) {
			params.mTop = surfaceHeight * Integer.parseInt(divMatch.group(4)) /100;
			params.mLeft = surfaceWidth * Integer.parseInt(divMatch.group(3)) /100;
			params.mWidth = surfaceWidth * Integer.parseInt(divMatch.group(1)) /100;
			params.mHeight = surfaceHeight * Integer.parseInt(divMatch.group(2)) /100;
		}
		Log.d(TAG, "getHarmonicSMPTEParams: " + params.mTop + ", " + params.mLeft + ", " + params.mWidth + ", " + params.mHeight);
		return params;
	}
}
