package com.movideo.baracus.model.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Metadata implements Serializable
{

	private static final long serialVersionUID = 1L;
	private String object;
	@SerializedName("data")
	private List<MetadataInfo> metadataList = new ArrayList<MetadataInfo>();

	public String getObject()
	{
		return this.object;
	}

	public void setObject(String object)
	{
		this.object = object;
	}

	public List<MetadataInfo> getMetadataList()
	{
		return metadataList;
	}

	public void setMetadataList(List<MetadataInfo> metadataList)
	{
		this.metadataList = metadataList;
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Metadata[").append("object : ").append(object).append(",\n").append("data : ").append(metadataList).append(",\n").append("]");
		return builder.toString();
	}

}
