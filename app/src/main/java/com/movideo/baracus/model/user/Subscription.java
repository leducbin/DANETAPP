package com.movideo.baracus.model.user;

import java.io.Serializable;
import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class Subscription implements Serializable
{

	private static final long serialVersionUID = 1L;
	private String title;
	@SerializedName("end_date")
	private Date endDate;
	@SerializedName("renewal_date")
	private Date renewalDate;
	private Payment payment;

	public String getTitle()
	{
		return this.title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public Date getEndDate()
	{
		return this.endDate;
	}

	public void setEndDate(Date endDate)
	{
		this.endDate = endDate;
	}

	public Date getRenewalDate()
	{
		return this.renewalDate;
	}

	public void setRenewalDate(Date renewalDate)
	{
		this.renewalDate = renewalDate;
	}

	public Payment getPayment()
	{
		return this.payment;
	}

	public void setPayment(Payment payment)
	{
		this.payment = payment;
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Subscription[").append("title : ").append(title).append(",\n").append("endDate : ").append(endDate).append(",\n").append("renewalDate : ").append(renewalDate).append(",\n").append("payment : ").append(payment).append(",\n").append("]");
		return builder.toString();
	}

}
