package com.movideo.baracus.model.media;

import java.io.Serializable;
import java.util.List;

public class Media implements Serializable
{
	private String productId;
	private String mediaId;
	private String baseUrl;
	private List<MediaStream> mediaStreams;
	private String authToken;
	private List<TextStream> textStreams = null;
	private String playerToken;
	private String licenseUrl;

	public String getMediaId()
	{
		return mediaId;
	}

	public void setMediaId(String mediaId)
	{
		this.mediaId = mediaId;
	}

	public String getBaseUrl()
	{
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl)
	{
		this.baseUrl = baseUrl;
	}

	public List<MediaStream> getMediaStreams()
	{
		return mediaStreams;
	}

	public void setMediaStreams(List<MediaStream> mediaStreams)
	{
		this.mediaStreams = mediaStreams;
	}

	public String getAuthToken()
	{
		return authToken;
	}

	public void setAuthToken(String authToken)
	{
		this.authToken = authToken;
	}

	public List<TextStream> getTextStreams()
	{
		return textStreams;
	}

	public void setTextStreams(List<TextStream> textStreams)
	{
		this.textStreams = textStreams;
	}

	public String getProductId()
	{
		return productId;
	}

	public void setProductId(String productId)
	{
		this.productId = productId;
	}

	public String getPlayerToken() {
		return playerToken;
	}

	public void setPlayerToken(String playerToken) {
		this.playerToken = playerToken;
	}

	public String getLicenseUrl() {
		return licenseUrl;
	}

	public void setLicenseUrl(String licenseUrl) {
		this.licenseUrl = licenseUrl;
	}

	public boolean isProtectedStream() {
		return licenseUrl != null;
	}
}