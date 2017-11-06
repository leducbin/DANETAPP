package com.movideo.baracus.model.collection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Collections implements Serializable
{

	private static final long serialVersionUID = 1L;
	private String object;
	private Integer count;
	@SerializedName("data")
	private List<Collection> collectionList = new ArrayList<Collection>();

	public Collections(String object, Integer count, List<Collection> collectionList)
	{
		this.object = object;
		this.count = count;
		this.collectionList = collectionList;

	}

	public String getObject()
	{
		return this.object;
	}

	public void setObject(String object)
	{
		this.object = object;
	}

	public Integer getCount()
	{
		return this.count;
	}

	public void setCount(Integer count)
	{
		this.count = count;
	}

	public List<Collection> getCollectionList()
	{
		return this.collectionList;
	}

	public void setCollectionList(List<Collection> collectionList)
	{
		this.collectionList = collectionList;
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Collection[")
				.append("object : ").append(object).append(",\n")
				.append("count : ").append(count).append(",\n")
				.append("data : ").append(collectionList).append(",\n").append("]");
		return builder.toString();
	}

}
