package com.movideo.baracus.model.product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Products implements Serializable
{

	private static final long serialVersionUID = 1L;
	private String object;
	private Integer count;
	@SerializedName("data")
	private List<Product> productList = new ArrayList<Product>();

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

	public List<Product> getProductList()
	{
		return productList;
	}

	public void setProductList(List<Product> productList)
	{
		Collections.sort(productList);
		this.productList = productList;
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Product[")
				.append("object : ").append(object).append(",\n")
				.append("count : ").append(count).append(",\n")
				.append("data : ").append(productList).append(",\n")
				.append("]");
		return builder.toString();
	}

}
