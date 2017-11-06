package com.movideo.baracus.xml;

import com.movideo.baracus.model.media.Media;
import com.movideo.baracus.model.media.MediaStream;
import com.movideo.baracus.model.media.TextStream;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * xml parser for Media SMIL xml
 *
 * @author rranawaka
 */
public class SmilSAXHandler extends SAXHandler<Media>
{
	private static final String ELEMENT_NAME_ENC_PROFILE = "encoding-profile-name";
	private static final String ELEMENT_NAME_WIDTH = "width";
	private static final String ELEMENT_NAME_HEIGHT = "height";
	private static final String ELEMENT_NAME_BITRATE = "system-bitrate";
	private static final String ELEMENT_NAME_TITLE = "title";
	private static final String ELEMENT_NAME_SRC = "src";
	private static final String ELEMENT_NAME_M3U8 = "m3u8";
	private static final String ELEMENT_NAME_AUDIO = "audio";
	private static final String ELEMENT_NAME_VIDEO = "video";
	private static final String ELEMENT_NAME_MPD = "mpd";
	private static final String ELEMENT_NAME_BASE = "base";
	private static final String ELEMENT_NAME_META = "meta";
	private static final String ELEMENT_NAME_NAME = "name";
	private static final String ELEMENT_NAME_CONTENT = "content";
	private static final String ELEMENT_VALUE_AUTH = "auth";
	private static final String ELEMENT_VALUE_LICENSE_URL = "licenseUrl";
	private static final String ELEMENT_VALUE_ID = "id";
	private static final String ELEMENT_NAME_TEXT_STREAM = "textstream";
	private static final String ELEMENT_NAME_LABEL = "label";

	private static Pattern pattern = Pattern.compile("^[a-zA-Z]+\\:\\/\\/");

	private Media media;

	public SmilSAXHandler()
	{
		super();
		media = new Media();
		result = media;
	}

	/*
	 *
	 * @see movideo.android.xml.handler.SAXHandler#startElementFound(java.lang. String, java.lang.String, java.lang.String,
	 * org.xml.sax.Attributes)
	 */
	@Override
	protected void startElementFound(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		if (qName.equals(ELEMENT_NAME_META))
		{
			String baseUrl = attributes.getValue(ELEMENT_NAME_BASE);
			if (baseUrl != null)
			{
				media.setBaseUrl(baseUrl);
			}

			String name = attributes.getValue(ELEMENT_NAME_NAME);
			if (name != null)
			{
				if (name.compareToIgnoreCase(ELEMENT_VALUE_AUTH) == 0)
				{
					String auth = attributes.getValue(ELEMENT_NAME_CONTENT);
					if (auth != null)
					{
						media.setAuthToken(auth);
					}
				}
				else if (name.compareToIgnoreCase(ELEMENT_VALUE_ID) == 0)
				{
					String id = attributes.getValue(ELEMENT_NAME_CONTENT);
					if (id != null)
					{
						media.setMediaId(id);
					}
				}
				else if (name.compareToIgnoreCase(ELEMENT_VALUE_LICENSE_URL) == 0)
				{
					String licenseUrl = attributes.getValue(ELEMENT_NAME_CONTENT);
					if (licenseUrl != null)
					{
						media.setLicenseUrl(licenseUrl);
					}
				}
			}
		}
		else if (qName.equals(ELEMENT_NAME_VIDEO) || qName.equals(ELEMENT_NAME_AUDIO) ||
				qName.equals(ELEMENT_NAME_M3U8) || qName.equals(ELEMENT_NAME_MPD))
		{
			MediaStream mediaStream = new MediaStream();
			if (media.getMediaStreams() == null)
			{
				media.setMediaStreams(new ArrayList<MediaStream>());
			}
			media.getMediaStreams().add(mediaStream);

			int len = attributes.getLength();
			for (int i = 0; i < len; i++)
			{
				String attrName = attributes.getQName(i);
				String attrValue = attributes.getValue(i);
				if (attrName != null && attrValue != null)
				{
					if (attrName.equals(ELEMENT_NAME_SRC))
					{
						String src = attrValue;
						Matcher matcher = pattern.matcher(src);
						if (!matcher.find())
						{
							src = media.getBaseUrl() + src;
						}
						mediaStream.setSource(src);
					}
					else if (attrName.equals(ELEMENT_NAME_BITRATE))
					{
						try
						{
							mediaStream.setBitrate(Long.parseLong(attrValue));
						} catch (Exception e)
						{
							throw new SAXException("Invalid bitrate in SMIL response. value = " + attrValue);
						}
					}
					else if (attrName.equals(ELEMENT_NAME_HEIGHT))
					{
						try
						{
							mediaStream.setHeight(Integer.parseInt(attrValue));
						} catch (Exception e)
						{
							throw new SAXException("Invalid height value in SMIL response. value = " + attrValue);
						}
					}
					else if (attrName.equals(ELEMENT_NAME_WIDTH))
					{
						try
						{
							mediaStream.setWidth(Integer.parseInt(attrValue));
						} catch (Exception e)
						{
							throw new SAXException("Invalid width value in SMIL response. value = " + attrValue);
						}
					}
					else if (attrName.equals(ELEMENT_NAME_ENC_PROFILE))
					{
						mediaStream.setEncodingProfile(attrValue);
					}
					else if (attrName.equals(ELEMENT_NAME_TITLE))
					{
						mediaStream.setTitle(attrValue);
					}
				}
			}
		}
		else if (qName.equals(ELEMENT_NAME_TEXT_STREAM))
		{
			String source = attributes.getValue(ELEMENT_NAME_SRC);
			if (source != null)
			{
				if (media.getTextStreams() == null)
				{
					media.setTextStreams(new ArrayList<TextStream>());
				}
				media.getTextStreams().add(new TextStream(source, attributes.getValue(ELEMENT_NAME_LABEL)));
			}
		}
	}

	/*
	 *
	 * @see movideo.android.xml.handler.SAXHandler#endElementFound(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	protected void endElementFound(String uri, String localName, String qName) throws SAXException
	{
	}

}