package com.movideo.whitelabel.widgetutils;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.widget.TextView;

import com.lifevibes.lvmediaplayer.LVSubtitle;
import com.lifevibes.lvmediaplayer.LVSubtitleCharAttributes;

/**
 * Class for subtitle formatting.<br>
 * The purpose of this class is to provide a method to process Subtile attributes<br>
 * like (foreground/background colors, transparency, font attributes,...) into<br>
 * a SpannableString directly usable in a Android TextView<br>
 */
public class LVClosedCaptionFormatter {

	//private static final String  TAG  =  "LVClosedCaptionFormatter"; // Tag used for Android logging

	/** Nested class that groups all Foreground formatting that can be applied */
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

			return retValue;
		}
	}

	/** Nested class that groups all Background formatting that can be applied */
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



	/** Specific formatter for CEA608 Subtitles*/
	public static void formatTextForCEA608Attributes(LVSubtitle subtitle, TextView textView)
	{
		String text = null;
		SpannableString spannableText = null;
		LVSubtitleCharAttributes[]  subCharAttr    = null;

		if (null != subtitle && null != textView)
		{
			text = subtitle.getTextPlanned();
			//DEBUG text = "LINE 1 : XXXXXXXXXXXXXXXXXXXXXXXDD\nLINE_2___XXXXXXXXXXXXXXXXXXXXXXXXX\nLine 3 :xxxxxxxxxxxxxxxxxxxxxxxxxx\nLine 4 :xxxxxxxxxxxxxxxxxxxxxxxxxx\nLine 5 :xxxxxxxxxxxxxxxxxxxxxxxxxx\nLine 6 :xxxxxxxxxxxxxxxxxxxxxxxxxx\nLine 7 :xxxxxxxxxxxxxxxxxxxxxxxxxx\nLine 8 :xxxxxxxxxxxxxxxxxxxxxxxxxx\nLine 9 :xxxxxxxxxxxxxxxxxxxxxxxxxx\nLine 10:xxxxxxxxxxxxxxxxxxxxxxxxxx\nLine 11:xxxxxxxxxxxxxxxxxxxxxxxxxx\nLine 12:xxxxxxxxxxxxxxxxxxxxxxxxxx\nLine 13:xxxxxxxxxxxxxxxxxxxxxxxxxx\nLine 14:xxxxxxxxxxxxxxxxxxxxxxxxxx\nLine 15:xxxxxxxxxxxxxxxxxxxxxxxxxx";
			subCharAttr = subtitle.getCharArrayAttributes();

			/** Test text not null*/
			if(null != text)
			{
				spannableText = new SpannableString(text);
				
				/** Test subtitle has attribute(s)*/
				if(null != subCharAttr)
				{
					applyCEA608BackgroundColor(spannableText, subCharAttr);
					applyCEA608ForegroundColor(spannableText, subCharAttr);
					applyCEA608FontStyle(spannableText, subCharAttr);
				}
				textView.setTypeface(Typeface.MONOSPACE);

				/** Stretch a little bit horizontally */
				textView.setTextScaleX(1.1f);
				textView.setText(spannableText);
			}
			else {
				textView.setText(null);
			}
		}
	}
	
	private static SpannableString applyCEA608FontStyle(SpannableString spannableText, LVSubtitleCharAttributes[] subCharAttr) {
		
		int idx;
		int attrIdx = 0;
		int spanStartId = 0;
		int spanEndId = 0;
		boolean startNewSpan = true;
		String text = spannableText.toString();
		
		// foreground style span
		byte currentFontStyle = 0x00;

		// foreground spans:
		/** Apply attributes character by character as long as text is big enough */
		for(idx = 0 ; idx < text.length() ; idx++)
		{
			if(Character.isWhitespace(text.charAt(idx))  /** check it is not a new line character */
					&& (Character.getType(text.charAt(idx)) == Character.CONTROL) ) /** control not usual whitespace */
			{
				spannableText.setSpan(ForegroundSpans.getFontStyleSpan(currentFontStyle)
						,spanStartId, spanEndId, Spannable.SPAN_COMPOSING);
				spannableText.setSpan(ForegroundSpans.getFontStrikethroughSpan(currentFontStyle)
						,spanStartId, spanEndId, Spannable.SPAN_COMPOSING);
				spannableText.setSpan(ForegroundSpans.getFontUnderlineSpan(currentFontStyle)
						,spanStartId, spanEndId, Spannable.SPAN_COMPOSING);
				
				startNewSpan = true;
				continue;
			}

			/** Check we have enough attributes to apply */
			if(attrIdx < subCharAttr.length)
			{
				if(startNewSpan) {
					startNewSpan = false;
					spanStartId = idx;
					spanEndId = idx;
					currentFontStyle = subCharAttr[attrIdx].getForegroundFontStyle();
				}

				if(subCharAttr[attrIdx].getForegroundFontStyle() != currentFontStyle) {
					
					spannableText.setSpan(ForegroundSpans.getFontStyleSpan(currentFontStyle)
							,spanStartId, spanEndId, Spannable.SPAN_COMPOSING);
					spannableText.setSpan(ForegroundSpans.getFontStrikethroughSpan(currentFontStyle)
							,spanStartId, spanEndId, Spannable.SPAN_COMPOSING);
					spannableText.setSpan(ForegroundSpans.getFontUnderlineSpan(currentFontStyle)
							,spanStartId, spanEndId, Spannable.SPAN_COMPOSING);
					
					startNewSpan = true;
				}

				if(startNewSpan) {
					startNewSpan = false;
					spanStartId = idx;
					spanEndId = idx;
					currentFontStyle = subCharAttr[attrIdx].getForegroundFontStyle();
				}
				attrIdx++;
			}
			spanEndId++;
		}
		//apply last span
		spannableText.setSpan(ForegroundSpans.getFontStyleSpan(currentFontStyle)
				,spanStartId, spanEndId, Spannable.SPAN_COMPOSING);
		
		spannableText.setSpan(ForegroundSpans.getFontStrikethroughSpan(currentFontStyle)
				,spanStartId, spanEndId, Spannable.SPAN_COMPOSING);

		spannableText.setSpan(ForegroundSpans.getFontUnderlineSpan(currentFontStyle)
				,spanStartId, spanEndId, Spannable.SPAN_COMPOSING);
		
		return spannableText;
	}


	private static SpannableString applyCEA608ForegroundColor(SpannableString spannableText, LVSubtitleCharAttributes[] subCharAttr) {
		
		int idx;
		int attrIdx = 0;
		int spanStartId = 0;
		int spanEndId = 0;
		boolean startNewSpan = true;
		String text = spannableText.toString();

		// foreground color span:
		int currentFgColor = 0;
		byte currentFgDisplayMode = 0;
		
		/** Apply attributes character by character as long as text is big enough */
		for(idx = 0 ; idx < text.length() ; idx++)
		{
			if(Character.isWhitespace(text.charAt(idx))  /** check it is not a new line character */
					&& (Character.getType(text.charAt(idx)) == Character.CONTROL) ) /** control not usual whitespace */
			{
				//apply span we got so far
				spannableText.setSpan(ForegroundSpans.getRGBColorSpan(currentFgDisplayMode, currentFgColor)
						,spanStartId, spanEndId, Spannable.SPAN_COMPOSING);

				startNewSpan = true;
				continue;
			}

			/** Check we have enough attributes to apply */
			if(attrIdx < subCharAttr.length)
			{
				if(startNewSpan) {
					startNewSpan = false;
					spanStartId = idx;
					spanEndId = idx;
					
					currentFgColor = subCharAttr[attrIdx].getForegroundColor();
					currentFgDisplayMode = subCharAttr[attrIdx].getForegroundDisplayMode();
				}

				if(subCharAttr[attrIdx].getForegroundColor() != currentFgColor ||
						subCharAttr[attrIdx].getForegroundDisplayMode() != currentFgDisplayMode) {
					
					spannableText.setSpan(ForegroundSpans.getRGBColorSpan(currentFgDisplayMode, currentFgColor)
							,spanStartId, spanEndId, Spannable.SPAN_COMPOSING);
					startNewSpan = true;
				}

				if(startNewSpan) {
					startNewSpan = false;
					spanStartId = idx;
					spanEndId = idx;

					currentFgColor = subCharAttr[attrIdx].getForegroundColor();
					currentFgDisplayMode = subCharAttr[attrIdx].getForegroundDisplayMode();
				}
				attrIdx++;
			}
			spanEndId++;
		}
		//apply last span
		spannableText.setSpan(ForegroundSpans.getRGBColorSpan(currentFgDisplayMode, currentFgColor)
				,spanStartId, spanEndId, Spannable.SPAN_COMPOSING);
		
		return spannableText;
	}

	private static SpannableString applyCEA608BackgroundColor(SpannableString spannableText,
			LVSubtitleCharAttributes[] subCharAttr)
	{
		int idx;
		int attrIdx = 0;
		int spanStartId = 0;
		int spanEndId = 0;
		boolean startNewSpan = true;
		
		int currentBgColor = 0;
		char currentBgTransparencyLevel = 0;
		byte currentBgDisplayMode = 0x00;
		String text = spannableText.toString();

		
		// background color and transparency:
		/** Apply attributes character by character as long as text is big enough */
		for(idx = 0 ; idx < text.length() ; idx++)
		{
			if(Character.isWhitespace(text.charAt(idx))  /** check it is not a new line character */
					&& (Character.getType(text.charAt(idx)) == Character.CONTROL) ) /** control not usual whitespace */
			{
				//apply span we got so far
				spannableText.setSpan(BackgroundSpans.getRGBColorSpan(currentBgDisplayMode,
						currentBgColor,
						currentBgTransparencyLevel)
						,spanStartId, spanEndId, Spannable.SPAN_COMPOSING);

				startNewSpan = true;
				continue;
			}

			/** Check we have enough attributes to apply */
			if(attrIdx < subCharAttr.length)
			{
				if(startNewSpan) {
					startNewSpan = false;
					spanStartId = idx;
					spanEndId = idx;

					currentBgColor = subCharAttr[attrIdx].getBackgoundColor();
					currentBgTransparencyLevel = subCharAttr[attrIdx].getBackgoundTransparencyLevel();
					currentBgDisplayMode = subCharAttr[attrIdx].getBackgoundDisplayMode();
				}

				if(subCharAttr[attrIdx].getBackgoundColor() != currentBgColor ||
						subCharAttr[attrIdx].getBackgoundTransparencyLevel() != currentBgTransparencyLevel ||
						subCharAttr[attrIdx].getBackgoundDisplayMode() != currentBgDisplayMode) {

					spannableText.setSpan(BackgroundSpans.getRGBColorSpan(currentBgDisplayMode,
							currentBgColor,
							currentBgTransparencyLevel)
							,spanStartId, spanEndId, Spannable.SPAN_COMPOSING);

					startNewSpan = true;
				}

				if(startNewSpan) {
					startNewSpan = false;
					spanStartId = idx;
					spanEndId = idx;

					currentBgColor = subCharAttr[attrIdx].getBackgoundColor();
					currentBgTransparencyLevel = subCharAttr[attrIdx].getBackgoundTransparencyLevel();
					currentBgDisplayMode = subCharAttr[attrIdx].getBackgoundDisplayMode();
				}
				attrIdx++;
			}
			spanEndId++;
		}
		//apply last span
		spannableText.setSpan(BackgroundSpans.getRGBColorSpan(currentBgDisplayMode,
				currentBgColor,
				currentBgTransparencyLevel)
				,spanStartId, spanEndId, Spannable.SPAN_COMPOSING);

		return spannableText;
	}
}
