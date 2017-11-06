package com.movideo.baracus.model.metadata;

import java.io.Serializable;

public class MetadataInfo implements Serializable
{

	private static final long serialVersionUID = 1L;
	private String type;
	private String value;

	public String getType()
	{
		return this.type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getValue()
	{
		return this.value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Data[").append("type : ").append(type).append(",\n").append("value : ").append(value).append(",\n").append("]");
		return builder.toString();
	}

}
