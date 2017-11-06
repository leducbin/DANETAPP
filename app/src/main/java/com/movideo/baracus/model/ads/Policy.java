package com.movideo.baracus.model.ads;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rranawaka on 3/12/2015.
 */
public class Policy
{
	private String provider;
	private String type;

	private List<List<String>> initial;
	private List<List<String>> recurring;

	public String getProvider()
	{
		return provider;
	}

	public void setProvider(String provider)
	{
		this.provider = provider;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public List<List<String>> getInitial() {
		return initial;
	}

	public void setInitial(List<List<String>> initial) {
		this.initial = initial;
	}

	public List<List<String>> getRecurring() {
		return recurring;
	}

	public void setRecurring(List<List<String>> recurring) {
		this.recurring = recurring;
	}
}
