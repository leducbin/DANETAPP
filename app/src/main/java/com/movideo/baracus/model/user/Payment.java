package com.movideo.baracus.model.user;

import java.io.Serializable;

public class Payment implements Serializable
{

	private static final long serialVersionUID = 1L;
	private String amount;
	private String currency;
	private String type;

	public String getAmount()
	{
		return this.amount;
	}

	public void setAmount(String amount)
	{
		this.amount = amount;
	}

	public String getCurrency()
	{
		return this.currency;
	}

	public void setCurrency(String currency)
	{
		this.currency = currency;
	}

	public String getType()
	{
		return this.type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Payment[").append("amount : ").append(amount).append(",\n").append("currency : ").append(currency).append(",\n").append("type : ").append(type).append(",\n").append("]");
		return builder.toString();
	}

}
