package com.movideo.baracus.model.product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Language implements Serializable
{

	private static final long serialVersionUID = 1L;
	private List<String> subtitles = new ArrayList<String>();
	private List<String> audios = new ArrayList<String>();

	public List<String> getSubtitles()
	{
		return this.subtitles;
	}

	public void setSubtitles(List<String> subtitles)
	{
		this.subtitles = subtitles;
	}

	public List<String> getAudios()
	{
		return this.audios;
	}

	public void setAudios(List<String> audios)
	{
		this.audios = audios;
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Language[").append("subtitles : ").append(subtitles).append(",\n").append("audios : ").append(audios).append(",\n").append("]");
		return builder.toString();
	}

}
