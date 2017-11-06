package com.movideo.baracus.model.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.movideo.baracus.model.product.Product;

public class Purchases implements Serializable
{

	private static final long serialVersionUID = 1L;
	private String object;
	private Integer count;
	@SerializedName("data")
	private List<Product> purchasedProducts = new ArrayList<Product>();

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

	public List<Product> getPurchasedProducts()
	{
		return purchasedProducts;
	}

	public void setPurchasedProducts(List<Product> purchasedProducts)
	{
		this.purchasedProducts = purchasedProducts;
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Device[").append("object : ").append(object).append(",\n").append("count : ").append(count).append(",\n").append("data : ").append(purchasedProducts).append(",\n").append("]");
		return builder.toString();
	}
}
