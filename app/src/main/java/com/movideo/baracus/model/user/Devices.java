package com.movideo.baracus.model.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Devices implements Serializable
{

	private static final long serialVersionUID = 1L;
	private String object;
	private Integer count;
	private String allowed;
	@SerializedName("data")
	private List<Device> device = new ArrayList<Device>();

	public String getObject()
	{
		return object;
	}

	public void setObject(String object)
	{
		this.object = object;
	}

	public Integer getCount()
	{
		return count;
	}

	public void setCount(Integer count)
	{
		this.count = count;
	}

	public String getAllowed()
	{
		return allowed;
	}

	public void setAllowed(String allowed)
	{
		this.allowed = allowed;
	}

	public List<Device> getDevice()
	{
		return device;
	}

	public void setDevice(List<Device> device)
	{
		this.device = device;
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Device[").append("object : ").append(object).append(",\n").append("count : ").append(count).append(",\n").append("allowed : ").append(allowed).append(",\n").append("data : ").append(device).append(",\n").append("]");
		return builder.toString();
	}
}
