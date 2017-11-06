package com.movideo.baracus.model.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.movideo.baracus.model.product.Product;

public class Favourite implements Serializable
{

	private static final long serialVersionUID = 1L;
	private String object;
	private Integer count;
	private Boolean success;
	@SerializedName("product_id")
	private String productId;
	@SerializedName("favourite_id")
	private String favouriteId;
	@SerializedName("data")
	private List<Product> favouriteProducts = new ArrayList<Product>();

	public String getFavouriteId()
	{
		return favouriteId;
	}

	public void setFavouriteId(String favouriteId)
	{
		this.favouriteId = favouriteId;
	}

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

	public Boolean getSuccess()
	{
		return success;
	}

	public void setSuccess(Boolean success)
	{
		this.success = success;
	}

	public String getProductId()
	{
		return productId;
	}

	public void setProductId(String productId)
	{
		this.productId = productId;
	}

	public List<Product> getFavouriteProducts()
	{
		return favouriteProducts;
	}

	public void setFavouriteProducts(List<Product> favouriteProducts)
	{
		this.favouriteProducts = favouriteProducts;
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Device[").append("object : ").append(object).append(",\n").append("count : ").append(count).append(",\n").append("success : ").append(success).append(",\n").append("productId : ").append(productId).append(",\n").append("favouriteId : ").append(favouriteId).append(",\n").append("data : ").append(favouriteProducts).append(",\n").append("]");
		return builder.toString();
	}
}
