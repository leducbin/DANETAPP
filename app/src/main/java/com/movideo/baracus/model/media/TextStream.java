package com.movideo.baracus.model.media;

import java.io.Serializable;

public class TextStream implements Serializable
{
	private String source;
	private String language;

	public TextStream()
	{
	}

	public TextStream(String source, String language)
	{
		this.source = source;
		this.language = language;
	}

	public String getSource()
	{
		return source;
	}

	public void setSource(String source)
	{
		this.source = source;
	}

	public String getLanguage()
	{
		return language;
	}

	public void setLanguage(String language)
	{
		this.language = language;
	}
}