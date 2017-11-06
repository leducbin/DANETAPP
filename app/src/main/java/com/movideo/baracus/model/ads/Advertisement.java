package com.movideo.baracus.model.ads;

import java.util.List;

/**
 * Created by rranawaka on 3/12/2015.
 */
public class Advertisement
{
	private String url;
	private List<CuePoint> cuePoints;
	private Policy policy;

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public List<CuePoint> getCuePoints()
	{
		return cuePoints;
	}

	public void setCuePoints(List<CuePoint> cuePoints)
	{
		this.cuePoints = cuePoints;
	}

	public Policy getPolicy() {
		return policy;
	}

	public void setPolicy(Policy policy) {
		this.policy = policy;
	}
}
