/*******************************************************************************
 * Copyright © Movideo Pty Limited 2013. All Rights Reserved
 ******************************************************************************/
package com.movideo.baracus.model.media;

import java.io.Serializable;

public class MediaStream implements IMediaFile, Serializable
{

	private String source;
	private long bitrate;
	private int width;
	private int height;
	private String encodingProfile;
	private String title;

	public String getSource()
	{
		return source;
	}

	public void setSource(String src)
	{
		this.source = src;
	}

	public long getBitrate()
	{
		return bitrate;
	}

	public void setBitrate(long bitrate)
	{
		this.bitrate = bitrate;
	}

	public int getWidth()
	{
		return width;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public int getHeight()
	{
		return height;
	}

	public void setHeight(int height)
	{
		this.height = height;
	}

	public String getEncodingProfile()
	{
		return encodingProfile;
	}

	public void setEncodingProfile(String encodingProfile)
	{
		this.encodingProfile = encodingProfile;
	}

	@Override
	public String toString()
	{
		return source + " : br = " + Long.toString(bitrate);
	}

	public String getQualityDesc()
	{
		return bitrate > 0 ? Long.toString(this.bitrate / 1000) : "";
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}
}
