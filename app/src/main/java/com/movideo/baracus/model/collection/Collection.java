package com.movideo.baracus.model.collection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.movideo.baracus.model.common.Image;
import com.movideo.baracus.model.product.Product;

public class Collection implements Serializable
{

	private static final long serialVersionUID = 1L;
	private Integer id;
	private String object;
	private String type;
	private String title;
	private String description;
	@SerializedName("total_count")
	private Integer count;
	@SerializedName("created_at")
	private Date createdDate;
	@SerializedName("modified_at")
	private Date modifiedDate;
	private Image image;
	@SerializedName("data")
	private List<Product> productList = new ArrayList<Product>();

	public Integer getId()
	{
		return this.id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public String getObject()
	{
		return this.object;
	}

	public void setObject(String object)
	{
		this.object = object;
	}

	public String getType()
	{
		return this.type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getTitle()
	{
		return this.title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public Integer getCount()
	{
		return this.count;
	}

	public void setCount(Integer count)
	{
		this.count = count;
	}

	public Date getCreatedDate()
	{
		return createdDate;
	}

	public void setCreatedDate(Date createdDate)
	{
		this.createdDate = createdDate;
	}

	public Date getModifiedDate()
	{
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate)
	{
		this.modifiedDate = modifiedDate;
	}

	public Image getImage()
	{
		return this.image;
	}

	public void setImage(Image image)
	{
		this.image = image;
	}

	public List<Product> getProductList()
	{
		return productList;
	}

	public void setProductList(List<Product> productList)
	{
		this.productList = productList;
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Data[")
				.append("id : ").append(id).append(",\n")
				.append("object : ").append(object).append(",\n")
				.append("type : ").append(type).append(",\n")
				.append("title : ").append(title).append(",\n")
				.append("description : ").append(description).append(",\n")
				.append("count : ").append(count).append(",\n")
				.append("image : ").append(image).append(",\n")
				.append("Created Date : ").append(createdDate).append(",\n")
				.append("Modified Date : ").append(modifiedDate).append(",\n")
                .append("]").append("Data : [,\n").append(productList + "\n  ]");
		return builder.toString();
	}

}
