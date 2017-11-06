/*******************************************************************************
 * Copyright © Movideo Pty Limited 2013. All Rights Reserved
 ******************************************************************************/
package com.movideo.baracus.model.media;

public interface IMediaFile
{
	public long getBitrate();

	public String getSource();

	public void setSource(String source);

	public int getWidth();

	public int getHeight();

	public void setWidth(int w);

	public void setHeight(int h);

	public String getQualityDesc();

	public String getTitle();
}
