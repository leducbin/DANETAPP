package com.movideo.baracus.model.common;

import java.io.Serializable;

public class ImageDetails implements Serializable
{

	private static final long serialVersionUID = 1L;
	private Integer width;
	private Integer height;
	private Boolean cropped;
	private String url;

	public ImageDetails(Integer width, Integer height, Boolean cropped, String url) {
		this.width = width;
		this.height = height;
		this.cropped = cropped;
		this.url = url;
	}

	public Integer getWidth()
	{
		return this.width;
	}

	public void setWidth(Integer width)
	{
		this.width = width;
	}

	public Integer getHeight()
	{
		return this.height;
	}

	public void setHeight(Integer height)
	{
		this.height = height;
	}

	public Boolean getCropped()
	{
		return this.cropped;
	}

	public void setCropped(Boolean cropped)
	{
		this.cropped = cropped;
	}

	public String getUrl()
	{
		return this.url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("\n{").append("width : ").append(width).append(",\n").append("height : ").append(height).append(",\n").append("cropped : ").append(cropped).append(",\n").append("url : ").append(url).append(",\n").append("}");
		return builder.toString();
	}

}
