package com.movideo.baracus.model.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class Image implements Serializable
{

	private static final long serialVersionUID = 1L;
	@SerializedName("base_uri")
	private String baseUri;
	private List<String> profiles = new ArrayList<String>();
	private Map<String, List<ImageDetails>> profile;

	public Map<String, List<ImageDetails>> getProfile()
	{
		return profile;
	}

	public void setProfile(Map<String, List<ImageDetails>> profile)
	{
		this.profile = profile;
	}

	public String getBaseUri()
	{
		return this.baseUri;
	}

	public void setBaseUri(String baseUri)
	{
		this.baseUri = baseUri;
	}

	public List<String> getProfiles()
	{
		return this.profiles;
	}

	public void setProfiles(List<String> profiles)
	{
		this.profiles = profiles;
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Images[").append("baseUri : ").append(baseUri).append(",\n").append("profiles : ").append(profiles).append(",\n").append("profile : ").append(profile).append(",\n").append("]");
		return builder.toString();
	}

}
