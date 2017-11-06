package com.movideo.baracus.model.product;

import java.io.Serializable;
import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class Offerings implements Serializable
{

	private static final long serialVersionUID = 1L;
	private String id;
	private String type;
	private String variant;
	@SerializedName("start_date")
	private Date startDate;
	@SerializedName("end_date")
	private Date endDate;
	private Double price;
	private String currency;
	private Boolean entitled;

	public String getId()
	{
		return this.id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getType()
	{
		return this.type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getVariant()
	{
		return this.variant;
	}

	public void setVariant(String variant)
	{
		this.variant = variant;
	}

	public Date getStartDate()
	{
		return this.startDate;
	}

	public void setStartDate(Date startDate)
	{
		this.startDate = startDate;
	}

	public Date getEndDate()
	{
		return this.endDate;
	}

	public void setEndDate(Date endDate)
	{
		this.endDate = endDate;
	}

	public Double getPrice()
	{
		return this.price;
	}

	public void setPrice(Double price)
	{
		this.price = price;
	}

	public String getCurrency()
	{
		return this.currency;
	}

	public void setCurrency(String currency)
	{
		this.currency = currency;
	}

	public Boolean getEntitled()
	{
		return this.entitled;
	}

	public void setEntitled(Boolean entitled)
	{
		this.entitled = entitled;
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Offerings[").append("id : ").append(id).append(",\n").append("type : ").append(type).append(",\n").append("variant : ").append(variant).append(",\n").append("startDate : ").append(startDate).append(",\n").append("endDate : ").append(endDate).append(",\n").append("price : ").append(price).append(",\n").append("currency : ").append(currency).append(",\n")
				.append("entitled : ").append(entitled).append(",\n").append("]");
		return builder.toString();
	}

}
